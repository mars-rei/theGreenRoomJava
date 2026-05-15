package androidev.thegreenroom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import android.net.Uri;
import java.util.UUID;

import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * About Fragment
 * The about section in the profile tab
 */
public class AboutFragment extends Fragment {

    private ImageView btnBack;
    private EditText editDescription;
    private Button btnAddTeaser;
    private Button btnSave;
    private ActivityResultLauncher<PickVisualMediaRequest> teaserPickerLauncher;
    private Uri teaserUri;
    private boolean photoSelected = false;

    // firestore resources
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private DataStoreManager dataStoreManager;
    private String userId;

    // for existing about section if there is
    private String existingAboutId = null;
    private String existingTeaserUrl = null;

    /**
     * On Create
     * Initialises Firestore, Cloud Storage, the Cloud Storage reference and DataStore
     * Initialises the photo picker for the Teaser
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialising
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        dataStoreManager = new DataStoreManager(requireContext());

        // initialising media picker launcher for photo
        teaserPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        photoSelected = true;
                        teaserUri = uri;
                    }
                }
        );
    }

    /**
     * On Create View
     * Converts the edit about XML file into View objects
     * Identifies objects in the XML file and stores them into variables
     * Gets user ID from DataStore
     * Loads the existing about section if user ID already exists
     * Sets up button click listeners
     * @return the inflated view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_about, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        editDescription = view.findViewById(R.id.edit_description);
        btnAddTeaser = view.findViewById(R.id.btn_add_teaser);
        btnSave = view.findViewById(R.id.btn_save);

        // get userId
        new Thread(() -> {
            userId = dataStoreManager.getUserIdBlocking();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // just ensures userId has been loaded in before checking if they have an existing about section
                    if (userId != null) {
                        loadExistingAboutSection();
                    }
                });
            }
        }).start();

        setupClickListeners();

        return view;
    }

    /**
     * Load Exiting About Section
     * Checks if the user already has an about section
     * If they do, their data is loaded into the about section UI
     */
    private void loadExistingAboutSection() {
        firestore.collection("users")
                .document(userId)
                .collection("about")
                .limit(1) // users have only one about section
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        AboutSection existingAbout = doc.toObject(AboutSection.class);

                        if (existingAbout != null) {
                            existingAboutId = doc.getId();

                            // put description into edittext
                            if (existingAbout.getDescription() != null && !existingAbout.getDescription().isEmpty()) {
                                editDescription.setText(existingAbout.getDescription());
                            }

                            // store existing teaser url
                            existingTeaserUrl = existingAbout.getTeaserUrl();
                        }
                    }
                });
    }

    /**
     * Setup Click Listeners
     * On click btnBack redirects to the previous activity
     * On click btnAddTeaser launches the photo picker for Teaser
     * On click btnSave gets the description EditText's value and checks if the user has made at least one edit
     * If the user has not made an edit, they are prompted to
     * If the user has selected a photo, uploadPhotoAndSave is called with description as a parameter
     * If the user has only changed the description, saveToFirestore is called with the description and already existing teaser url as parameters
     * Loads the existing about section if user ID already exists
     * Sets up button click listeners
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // add teaser to about
        btnAddTeaser.setOnClickListener(v -> {
            launchTeaserPicker();
        });

        // save about details
        btnSave.setOnClickListener(v -> {
            String description = editDescription.getText().toString().trim();

            // make sure user makes at least one edit
            if (description.isEmpty() && !photoSelected && existingTeaserUrl == null) {
                Toast.makeText(getContext(), "Add a description or a teaser image", Toast.LENGTH_LONG).show();
                return;
            }

            if (photoSelected) {
                uploadPhotoAndSave(description);
            } else {
                saveToFirestore(description, existingTeaserUrl);
            }
        });
    }

    /**
     * Upload Photo And Save
     * The Teaser is given a unique filename and storage reference to be stored to Cloud Storage
     * saveToFirestore is called with the description and Teaser uri as parameters
     * @param description (String)
     */
    private void uploadPhotoAndSave(String description) {
        String fileName = "about_teasers/" + userId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference teaserRef = storageRef.child(fileName);

        teaserRef.putFile(teaserUri)
                .addOnSuccessListener(taskSnapshot -> {
                    teaserRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToFirestore(description, uri.toString());
                    });
                });
    }

    /**
     * Save To Firestore
     * The existing / newly randomly generated about section ID is stored in aboutId
     * A new AboutSection object is created using the aboutId, userId, description and teaserUrl
     * This object is then stored to Firestore's "about" subcollection in the "users" collection
     * On success the user is redirected to the profile tab
     * @param description (String)
     * @param teaserUrl (String)
     */
    private void saveToFirestore(String description, String teaserUrl) {
        String aboutId;

        // use existing id if editing existing about section
        if (existingAboutId != null) {
            aboutId = existingAboutId;
        } else {
            aboutId = UUID.randomUUID().toString();
        }

        // create about object via model
        AboutSection about = new AboutSection(aboutId, userId, description, teaserUrl);

        // save to firestore
        firestore.collection("users")
                .document(userId)
                .collection("about")
                .document(aboutId)
                .set(about)
                .addOnSuccessListener(aVoid -> {
                    // go back to profile
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }

    /**
     * Launch Teaser Picker
     * The teaserPickerLauncher is launched to allow the user to select a photo from their device
     */
    private void launchTeaserPicker() {
        teaserPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}