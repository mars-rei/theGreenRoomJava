package androidev.thegreenroom;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedFragment extends Fragment {

    private LinearLayout postsContainer;
    private FirebaseFirestore firestore;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        postsContainer = view.findViewById(R.id.postsContainer);

        firestore = FirebaseFirestore.getInstance();

        loadAllPosts();

        return view;
    }

    private void loadAllPosts() {
        postsContainer.removeAllViews();

        // using collection group query to query within user collection's showcase subcollection
        firestore.collectionGroup("showcase")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<DocumentSnapshot> postDocuments = queryDocumentSnapshots.getDocuments();

                    // get unique user ids from posts
                    List<String> userIds = new ArrayList<>();
                    for (DocumentSnapshot document : postDocuments) {
                        String userId = getUserIdFromPostDocument(document);
                        if (!userIds.contains(userId)) {
                            userIds.add(userId);
                        }
                    }

                    // load all user data
                    loadUsersData(userIds, postDocuments);
                });
    }

    private String getUserIdFromPostDocument(DocumentSnapshot document) {
        // get user id -> users/{userId}/showcase/{postId}
        String path = document.getReference().getPath();
        String[] sections = path.split("/");

        // gets user id from users collection
        if (sections.length >= 2) {
            return sections[1];
        }
        return null;
    }

    private void loadUsersData(List<String> userIds, List<DocumentSnapshot> postDocuments) {
        final int[] usersLoaded = {0};
        Map<String, User> users = new HashMap<>();

        for (String userId : userIds) {
            // load user data
            firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            users.put(userId, user);
                        }

                        usersLoaded[0]++;
                        if (usersLoaded[0] == userIds.size()) {
                            displayPosts(postDocuments, users);
                        }
                    });
        }
    }

    private void displayPosts(List<DocumentSnapshot> postDocuments, Map<String, User> usersMap) {
        for (DocumentSnapshot post : postDocuments) {
            ShowcasePost showcasePost = post.toObject(ShowcasePost.class);
            String userId = getUserIdFromPostDocument(post);
            User user = usersMap.get(userId);
            addPostToFeed(showcasePost, user);
        }
    }

    private void addPostToFeed(ShowcasePost post, User user) {
        View postView = LayoutInflater.from(getContext())
                .inflate(R.layout.post_card_template, postsContainer, false);

        // user data
        ImageView userProfileImage = postView.findViewById(R.id.profileImage);
        TextView userNameText = postView.findViewById(R.id.username);
        TextView userLocationText = postView.findViewById(R.id.location);

        // post data
        ImageView postImage = postView.findViewById(R.id.image);
        TextView postTitle = postView.findViewById(R.id.title);
        TextView postDescription = postView.findViewById(R.id.description);

        // use user data
        userNameText.setText(user.getUsername());
        userLocationText.setText(user.getLocation());
        Glide.with(this)
                .load(user.getProfilePictureUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(userProfileImage);

        // use post data
        postTitle.setText(post.getTitle());
        postDescription.setText(post.getDescription());
        Glide.with(this)
                .load(post.getPictureUrl())
                .placeholder(R.drawable.profile_placeholder)
                .into(postImage);

        postsContainer.addView(postView);
    }
}