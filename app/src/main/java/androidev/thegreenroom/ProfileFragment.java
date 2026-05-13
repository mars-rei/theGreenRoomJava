package androidev.thegreenroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private ImageView headerPhoto, profilePhoto;
    private TextView username, location, bio;
    private Button editProfileButton;

    private FirebaseFirestore firestore;
    private DataStoreManager dataStoreManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        headerPhoto = view.findViewById(R.id.headerPhoto);
        profilePhoto = view.findViewById(R.id.profilePicture);
        username = view.findViewById(R.id.username);
        location = view.findViewById(R.id.location);
        bio = view.findViewById(R.id.bio);

        editProfileButton = view.findViewById(R.id.btn_edit_profile);

        // initialise
        dataStoreManager = new DataStoreManager(requireContext());
        firestore = FirebaseFirestore.getInstance();

        // call method to load user data
        loadUserData();

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileEditFragment profileEditFragment = new ProfileEditFragment();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.flFragment, profileEditFragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }

    // load user data from datastore (user id) and firestore (to display)
    private void loadUserData() {
        // had to be moved to the background rather than main thread
        new Thread(() -> {
            // get user id
            String userId = dataStoreManager.getUserIdBlocking();

            // get user data from firestore
            firestore.collection("users")
                    .whereEqualTo("id", userId)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult().getDocuments().get(0);
                            User user = document.toObject(User.class);
                            requireActivity().runOnUiThread(() -> displayUserData(user));
                        }
                    });
        }).start();
    }

    private void displayUserData(User user) {
        // set text
        username.setText(user.getUsername());
        location.setText(user.getLocation());
        bio.setText(user.getBio());

        // load header / banner photo using glide
        if (user.getHeaderPhotoUrl() != null && !user.getHeaderPhotoUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getHeaderPhotoUrl())
                    .into(headerPhoto);
        }

        // load profile picture using glide
        if (user.getProfilePictureUrl() != null && !user.getProfilePictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfilePictureUrl())
                    .into(profilePhoto);
        }
    }
}