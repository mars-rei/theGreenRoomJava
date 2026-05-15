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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class ShowcaseFragment extends Fragment {

    private ImageView btnBack;
    private EditText editTitle;
    private EditText editDescription;
    private Button btnAddMedia;
    private Button btnPost;

    private ActivityResultLauncher<PickVisualMediaRequest> mediaPickerLauncher;
    private Uri photoUri;
    private boolean photoSelected = false;

    // firestore resources
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private DataStoreManager dataStoreManager;
    private String userId;

    /**
     * On Create
     * Initialises Firestore, Cloud Storage, reference for Cloud Storage and DataStore
     * Initialises a photo picker
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
        mediaPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        photoSelected = true;
                        photoUri = uri;
                    }
                }
        );
    }

    /**
     * On Create View
     * Converts the fragment add showcase XML file into View objects
     * Gets user id
     * Calls setupClickListeners
     * @return add to showcase view
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_showcase, container, false);

        btnBack = view.findViewById(R.id.btn_back);
        editTitle = view.findViewById(R.id.edit_title);
        editDescription = view.findViewById(R.id.edit_bio);
        btnAddMedia = view.findViewById(R.id.btn_add_media);
        btnPost = view.findViewById(R.id.btn_post);

        // get userId
        new Thread(() -> {
            userId = dataStoreManager.getUserIdBlocking();
        }).start();

        setupClickListeners();

        return view;
    }

    /**
     * Setup Click Listeners
     * Sets click listeners for back, add media and post buttons
     * Validates input on post and calls uploadPhotoAndSave with title and description as parameters
     */
    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().popBackStack();
        });

        // add media to showcase post (limited to one photo for now)
        btnAddMedia.setOnClickListener(v -> {
            launchMediaPicker();
        });

        // post and save
        btnPost.setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (title.isEmpty()) {
                editTitle.setError("Title is required");
                return;
            }

            if (description.isEmpty()) {
                editDescription.setError("Description is required");
                return;
            }

            if (!photoSelected || photoUri == null) {
                Toast.makeText(getContext(), "Please select a photo first", Toast.LENGTH_SHORT).show();
                return;
            }

            uploadPhotoAndSave(title, description);
        });
    }

    /**
     * Upload Photo And Save
     * Generates a unique file name to use as reference
     * Stores photo to Cloud Storage
     * Calls saveToFirestore with title, description, and the photo's uri as parameters
     * @param title (String)
     * @param description (String)
     */
    private void uploadPhotoAndSave(String title, String description) {
        String fileName = "showcase_photos/" + userId + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference photoRef = storageRef.child(fileName);

        photoRef.putFile(photoUri)
                .addOnSuccessListener(taskSnapshot -> {
                    photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        saveToFirestore(title, description, uri.toString());
                    });
                });
    }

    /**
     * Save To Firestore
     * Generates a unique id for showcase post
     * Creates an object using parameter data
     * Saves object to "showcase" subcollection in "users" collection in Firestore
     * Redirects user to profile
     * @param title (String)
     * @param description (String)
     * @param photoUrl (String)
     */
    private void saveToFirestore(String title, String description, String photoUrl) {

        // generate unique id for post
        String postId = UUID.randomUUID().toString();

        // create showcase object via model
        ShowcasePost post = new ShowcasePost(postId, userId, title, description, photoUrl);

        // save to firestore
        firestore.collection("users")
                .document(userId)
                .collection("showcase")
                .document(postId)
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    // go back to profile
                    requireActivity().getSupportFragmentManager().popBackStack();
                });
    }

    /**
     * Launch Media Picker
     * Launches media picker
     */
    private void launchMediaPicker() {
        mediaPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}