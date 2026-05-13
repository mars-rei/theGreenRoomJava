package androidev.thegreenroom;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.List;

public class ProfileFragment extends Fragment {

    private ImageView headerPhoto, profilePhoto;
    private TextView username, location, bio;
    private Button editProfileButton;

    // for tabs
    private TextView showcaseTab, aboutTab, scheduleTab;
    private LinearLayout contentContainer;
    private ScrollView scrollView;

    // for empty tab layouts
    private View showcaseEmptyLayout;

    // for filled tab layouts
    private View showcaseLayout;
    private View aboutLayout;
    private View scheduleLayout;

    private FirebaseFirestore firestore;
    private DataStoreManager dataStoreManager;
    private String currentUserId;

    // to refresh
    @Override
    public void onResume() {
        super.onResume();
        if (showcaseTab.getTypeface() != null &&
                showcaseTab.getTypeface().isBold()) {
            loadShowcasePosts();
        }
    }

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

        // for tabs
        showcaseTab = view.findViewById(R.id.showcase);
        aboutTab = view.findViewById(R.id.about);
        scheduleTab = view.findViewById(R.id.schedule);
        contentContainer = view.findViewById(R.id.profile_section_container);
        scrollView = view.findViewById(R.id.profileSectionView);

        // inflate empty tab layouts
        showcaseEmptyLayout = inflater.inflate(R.layout.fragment_empty_profile_showcase, contentContainer, false);

        // inflate filled tab layouts
        showcaseLayout = inflater.inflate(R.layout.fragment_filled_profile_showcase, contentContainer, false);
        aboutLayout = inflater.inflate(R.layout.fragment_empty_profile_about, contentContainer, false);
        scheduleLayout = inflater.inflate(R.layout.fragment_empty_profile_schedule, contentContainer, false);

        tabListeners();
        aboutTab(); // go to about tab as default

        // initialise
        dataStoreManager = new DataStoreManager(requireContext());
        firestore = FirebaseFirestore.getInstance();

        // load user id and user data
        load();

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

    private void load() {
        new Thread(() -> {
            String userId = dataStoreManager.getUserIdBlocking();
            requireActivity().runOnUiThread(() -> {
                currentUserId = userId;
                loadUserData();
            });
        }).start();
    }

    // tabs
    private void tabListeners() {
        showcaseTab.setOnClickListener(v -> showcaseTab());
        aboutTab.setOnClickListener(v -> aboutTab());
        scheduleTab.setOnClickListener(v -> scheduleTab());
    }

    private void showcaseTab() {
        // update tab heading styles
        showcaseTab.setTypeface(null, Typeface.BOLD);
        aboutTab.setTypeface(null, Typeface.NORMAL);
        scheduleTab.setTypeface(null, Typeface.NORMAL);

        contentContainer.removeAllViews();

        // load posts
        loadShowcasePosts();
    }

    private void loadShowcasePosts() {
        firestore.collection("users")
                .document(currentUserId)
                .collection("showcase")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> posts = queryDocumentSnapshots.getDocuments();

                    contentContainer.removeAllViews();

                    if (posts.isEmpty()) {
                        // show empty xml file if no posts
                        contentContainer.addView(showcaseEmptyLayout);

                        Button btnAddShowcase = showcaseEmptyLayout.findViewById(R.id.btn_edit_showcase);
                        btnAddShowcase.setOnClickListener(v -> addShowcase());
                    } else {
                        // show filled version
                        contentContainer.addView(showcaseLayout);

                        Button btnAddShowcase = showcaseLayout.findViewById(R.id.btn_add_showcase);
                        btnAddShowcase.setOnClickListener(v -> addShowcase());

                        LinearLayout showcaseContainer = showcaseLayout.findViewById(R.id.showcase_container);
                        showcaseContainer.removeAllViews();

                        // for each post in firestore, add post to showcase display
                        for (DocumentSnapshot document : posts) {
                            ShowcasePost post = document.toObject(ShowcasePost.class);
                            if (post != null) {
                                updateShowcaseSection(showcaseContainer, post);
                            }
                        }
                    }

                    // stay at top of scroll
                    scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
                });
    }

    private void updateShowcaseSection(LinearLayout container, ShowcasePost post) {
        View postView = LayoutInflater.from(getContext())
                .inflate(R.layout.showcase_card_template, container, false);

        ImageView image = postView.findViewById(R.id.image);
        TextView title = postView.findViewById(R.id.title);
        TextView description = postView.findViewById(R.id.description);

        title.setText(post.getTitle());
        description.setText(post.getDescription());

        if (post.getPictureUrl() != null && !post.getPictureUrl().isEmpty()) {
            Glide.with(this)
                    .load(post.getPictureUrl())
                    .placeholder(R.drawable.profile_placeholder)
                    .into(image);
        }

        container.addView(postView);
    }

    private void addShowcase() {
        ShowcaseFragment showcaseFragment = new ShowcaseFragment();
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.flFragment, showcaseFragment)
                .addToBackStack(null)
                .commit();
    }


    private void aboutTab() {
        // update tab heading styles
        showcaseTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        aboutTab.setTypeface(null, android.graphics.Typeface.BOLD);
        scheduleTab.setTypeface(null, android.graphics.Typeface.NORMAL);

        contentContainer.removeAllViews();
        contentContainer.addView(aboutLayout);

        Button btnEditAbout = aboutLayout.findViewById(R.id.btn_edit_about);
        btnEditAbout.setOnClickListener(v -> {
            // TODO
        });

        // stay at top of scroll
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
    }

    private void scheduleTab() {
        // update tab heading styles
        showcaseTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        aboutTab.setTypeface(null, android.graphics.Typeface.NORMAL);
        scheduleTab.setTypeface(null, android.graphics.Typeface.BOLD);

        contentContainer.removeAllViews();
        contentContainer.addView(scheduleLayout);

        Button btnCreateEvent = scheduleLayout.findViewById(R.id.btn_edit_schedule);
        btnCreateEvent.setOnClickListener(v -> {
            // TODO
        });

        // stay at top of scroll
        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_UP));
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