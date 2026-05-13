package androidev.thegreenroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// for media picker
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import android.util.Log;
import android.net.Uri;

// for firebase resources
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

// to generate a unique id since auth hasn't been integrated yet
import java.util.UUID;

public class ProfileEditFragment extends Fragment {

    private ImageView headerPhoto, profilePhoto;
    private EditText username, location, bio;
    private Button saveButton;

    private ActivityResultLauncher<PickVisualMediaRequest> headerPhotoPickerLauncher;
    private ActivityResultLauncher<PickVisualMediaRequest> profilePhotoPickerLauncher;

    // firestore resources
    private FirebaseFirestore firestore;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    // cloud storage
    private Uri headerPhotoUri;
    private Uri profilePhotoUri;
    private String headerPhotoUrl = "";
    private String profilePhotoUrl = "";


    // to set onboardingComplete flag true when user saves
    private DataStoreManager dataStoreManager;

    // if user is just re-editing profile
    private String currentUsername, currentLocation, currentBio;
    private String currentHeaderPhotoUrl, currentProfilePhotoUrl;
    private String currentUserId;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialising firebase resources
        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // initialising datastore
        dataStoreManager = new DataStoreManager(requireContext());

        // initialising photo picker launcher for header banner / photo
        headerPhotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        Log.d("ProfileEditFragment", "Selected header photo URI: " + uri);
                        headerPhotoUri = uri;
                        headerPhoto.setImageURI(uri);
                    } else {
                        Log.d("ProfileEditFragment", "No header photo selected");
                    }
                }
        );

        // initialising photo picker launcher for profile photo
        profilePhotoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        Log.d("ProfileEditFragment", "Selected profile photo URI: " + uri);
                        profilePhotoUri = uri;
                        profilePhoto.setImageURI(uri);
                    } else {
                        Log.d("ProfileEditFragment", "No profile photo selected");
                    }
                }
        );
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        headerPhoto = view.findViewById(R.id.editHeaderPhoto);
        profilePhoto = view.findViewById(R.id.editProfilePicture);

        username = view.findViewById(R.id.editUsername);
        location = view.findViewById(R.id.editLocation);
        bio = view.findViewById(R.id.editBio);

        saveButton = view.findViewById(R.id.btn_save);


        headerPhoto.setOnClickListener(v -> launchHeaderPhotoPicker());
        profilePhoto.setOnClickListener(v -> launchProfilePhotoPicker());

        // on save, save profile
        saveButton.setOnClickListener(v -> saveProfile());

        // for if user is re-editing profile
        loadCurrentUserData();

        return view;
    }

    // load current user data if onboarding has already been completed
    private void loadCurrentUserData() {
        new Thread(() -> {
            currentUserId = dataStoreManager.getUserIdBlocking();

            if (currentUserId != null && !currentUserId.isEmpty()) {
                firestore.collection("users")
                        .document(currentUserId)
                        .get()
                        .addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists()) {
                                User user = documentSnapshot.toObject(User.class);
                                if (user != null) {
                                    // make current data appear instead of placeholders and blank edit text fields
                                    requireActivity().runOnUiThread(() -> setCurrentProfile(user));
                                }
                            }
                        });
            }
        }).start();
    }

    private void setCurrentProfile(User user) {
        // get current values
        currentUsername = user.getUsername();
        currentLocation = user.getLocation();
        currentBio = user.getBio();
        currentHeaderPhotoUrl = user.getHeaderPhotoUrl();
        currentProfilePhotoUrl = user.getProfilePictureUrl();

        // set hint text for edit text views
        username.setHint(currentUsername);
        location.setHint(currentLocation);
        bio.setHint(currentBio);

        // load existing photos
        if (currentHeaderPhotoUrl != null && !currentHeaderPhotoUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentHeaderPhotoUrl)
                    .placeholder(R.drawable.header_placeholder)
                    .into(headerPhoto);
        } else {
            headerPhoto.setImageResource(R.drawable.header_placeholder);
        }

        if (currentProfilePhotoUrl != null && !currentProfilePhotoUrl.isEmpty()) {
            Glide.with(this)
                    .load(currentProfilePhotoUrl)
                    .placeholder(R.drawable.profile_placeholder)
                    .into(profilePhoto);
        } else {
            profilePhoto.setImageResource(R.drawable.profile_placeholder);
        }
    }

    // begin saving profile method - getting all data to be saved
    private void saveProfile() {
        // get values
        String profileUsername = username.getText().toString().trim();
        String profileLocation = location.getText().toString().trim();
        String profileBio = bio.getText().toString().trim();

        // input validation
        if (profileUsername.isEmpty()) {
            if (currentUsername != null && !currentUsername.isEmpty()) {
                profileUsername = currentUsername;
            } else {
                username.setError("Username is required");
                return;
            }
        }

        if (profileLocation.isEmpty()) {
            if (currentLocation != null && !currentLocation.isEmpty()) {
                profileLocation = currentLocation;
            } else {
                location.setError("Location is required");
                return;
            }
        }

        if (profileBio.isEmpty()) {
            if (currentBio != null && !currentBio.isEmpty()) {
                profileBio = currentBio;
            } else {
                bio.setError("Bio is required");
                return;
            }
        }

        // get userType from DataStore
        String userType = dataStoreManager.getUserTypeBlocking();

        // generate unique user id if no user id yet
        String userId;
        if (currentUserId != null && !currentUserId.isEmpty()) {
            userId = currentUserId;
        } else {
            userId = UUID.randomUUID().toString();
        }

        // pass user data to upload images and then save
        uploadImagesAndSave(userId, userType, profileUsername, profileLocation, profileBio);
    }


    // to upload images to cloud storage and save other details to firestore
    private void uploadImagesAndSave(String userId, String userType, String username,
                                     String location, String bio) {
        // counter for num uploads
        final int[] uploadsCompleted = {0};
        final int[] numUploads = {0};

        if (headerPhotoUri != null) numUploads[0]++;
        if (profilePhotoUri != null) numUploads[0]++;

        // uses current header or profile if not changed
        final String[] headerUrl = {currentHeaderPhotoUrl != null ? currentHeaderPhotoUrl : ""};
        final String[] profileUrl = {currentProfilePhotoUrl != null ? currentProfilePhotoUrl : ""};

        // save directly if no image uploads needed
        if (numUploads[0] == 0) {
            saveUserToFirestore(userId, userType, username, location, bio,
                    profileUrl[0], headerUrl[0]);
            return;
        }

        // uploading header photo
        if (headerPhotoUri != null) {

            String fileName = "header_photos/" + userId + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference photoRef = storageRef.child(fileName);

            photoRef.putFile(headerPhotoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            headerUrl[0] = uri.toString();
                            uploadsCompleted[0]++;

                            // check if uploads are complete
                            if (uploadsCompleted[0] == numUploads[0]) {
                                saveUserToFirestore(userId, userType, username, location, bio,
                                        profileUrl[0], headerUrl[0]);
                            }
                        });
                    });
        }

        // uploading profile picture
        if (profilePhotoUri != null) {
            // configure storage path using user id and time
            String fileName = "profile_photos/" + userId + "_" + System.currentTimeMillis() + ".jpg";
            StorageReference photoRef = storageRef.child(fileName);

            photoRef.putFile(profilePhotoUri)
                    .addOnSuccessListener(taskSnapshot -> {
                        photoRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            profileUrl[0] = uri.toString();
                            uploadsCompleted[0]++;

                            // check if uploads are complete
                            if (uploadsCompleted[0] == numUploads[0]) {
                                saveUserToFirestore(userId, userType, username, location, bio,
                                        profileUrl[0], headerUrl[0]);
                            }
                        });
                    });
        }
    }

    private void saveUserToFirestore(String userId, String userType, String username,
                                     String location, String bio,
                                     String profilePictureUrl, String headerPhotoUrl) {
        // create user instance from class
        User user = new User(userId, userType, username, bio, location,
                profilePictureUrl, headerPhotoUrl);

        // save to firestore
        firestore.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    // save user id to datastore - user is likely to only have one account
                    dataStoreManager.setUserId(userId);

                    // set onboarding complete flag to true
                    dataStoreManager.setOnboardingComplete(true);


                    // switch to the right profile fragment (readable)
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).switchToReadableProfile();
                    }

                    if (getActivity() instanceof MainActivity) {
                        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigationView);
                        bottomNav.setSelectedItemId(R.id.profile);
                    }
                })
        ;
    }

    private void launchHeaderPhotoPicker() {
        headerPhotoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void launchProfilePhotoPicker() {
        profilePhotoPickerLauncher.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }
}