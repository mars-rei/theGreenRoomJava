package androidev.thegreenroom;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.Bundle;
import android.view.LayoutInflater;
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

    /**
     * On Create View
     * Converts the fragment feed XML file into View objects
     * Initialises Firestore
     * Calls loadAllPosts
     * @return the feed view with all loaded posts
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_feed, container, false);

        postsContainer = view.findViewById(R.id.posts_container);

        firestore = FirebaseFirestore.getInstance();

        loadAllPosts();

        return view;
    }

    /**
     * Load All Posts
     * Uses a collection group query to get all posts from the "showcase" subcollection in "users"
     * Along with posts, gets user data to form the UI post cards
     * Calls loadUsersData with userIds and postDocuments as parameters
     */
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
                        String userId = getUserId(document);
                        if (!userIds.contains(userId)) {
                            userIds.add(userId);
                        }
                    }

                    // load all user data
                    loadUsersData(userIds, postDocuments);
                });
    }

    /**
     * Get User Id
     * Retrieves the user id from a document
     * @param document (DocumentSnapshot)
     * @return user id
     */
    private String getUserId(DocumentSnapshot document) {
        // get user id -> users/{userId}/showcase/{postId}
        String path = document.getReference().getPath();
        String[] sections = path.split("/");

        // gets user id from users collection
        return sections[1];
    }

    /**
     * Load Users Data
     * For every user id, load their document into a User object and store in a hashmap with their user id
     * Once all users have been loaded, call displayPosts with postDocuments and users as parameters
     * @param userIds (list of String)
     * @param postDocuments (list of DocumentSnapshot)
     */
    private void loadUsersData(List<String> userIds, List<DocumentSnapshot> postDocuments) {
        final int[] usersLoaded = {0};
        Map<String, User> users = new HashMap<>();

        for (String userId : userIds) {
            firestore.collection("users")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(doc -> {
                        User user = doc.toObject(User.class);
                        users.put(userId, user);

                        usersLoaded[0]++;
                        if (usersLoaded[0] == userIds.size()) {
                            displayPosts(postDocuments, users);
                        }
                    });
        }
    }

    /**
     * Display Posts
     * For every post in postDocuments, convert document into a ShowcasePost object
     * Gets the user id from the post and retrieves the user with that user id
     * Calls addPostToFeed with showcasePost and user as parameters
     * @param postDocuments (list of DocumentSnapshot)
     * @param users (map of String and User)
     */
    private void displayPosts(List<DocumentSnapshot> postDocuments, Map<String, User> users) {
        for (DocumentSnapshot post : postDocuments) {
            ShowcasePost showcasePost = post.toObject(ShowcasePost.class);
            String userId = getUserId(post);
            User user = users.get(userId);
            addPostToFeed(showcasePost, user);
        }
    }

    /**
     * Add Post To Feed
     * Replaces text and image values in the post card template XML file
     * Adds this card to the posts container
     * @param post (ShowcasePost)
     * @param user (User)
     */
    private void addPostToFeed(ShowcasePost post, User user) {
        View postView = LayoutInflater.from(getContext())
                .inflate(R.layout.post_card_template, postsContainer, false);

        // user data
        ImageView userProfileImage = postView.findViewById(R.id.profile_image);
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