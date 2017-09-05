package com.example.patearn.patearnfirebase;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.patearn.patearnfirebase.Tabs.Tab1Abs;
import com.example.patearn.patearnfirebase.Tabs.Tab2Attor;
import com.example.patearn.patearnfirebase.Tabs.Tab3Etc;
import com.facebook.login.LoginManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

//    private static final int GALLERY_CODE = 10;
    private TextView nameTextView;
    private TextView emailTextView;
    private FirebaseAuth mAuth;
//    private FirebaseStorage storage;
//    private ImageView imageView;
//    private EditText title;
//    private EditText description;
//    private Button button;
//    private String imagePath;
//    private FirebaseDatabase database;
    private FirebaseRemoteConfig mFirebaseRemoteConfig;
    private Toolbar toolbar;
    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();
//        storage = FirebaseStorage.getInstance();
//        database = FirebaseDatabase.getInstance();
//        imageView = (ImageView) findViewById(R.id.imageView);
//        title = (EditText) findViewById(R.id.title);
//        description = (EditText) findViewById(R.id.description);
        //button = (Button) findViewById(R.id.button);

        //권한 부여
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},0);
        }

        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View view = navigationView.getHeaderView(0);

        nameTextView = view.findViewById(R.id.header_name_textView);
        emailTextView = view.findViewById(R.id.header_email_textView);
        nameTextView.setText(mAuth.getCurrentUser().getDisplayName());
        emailTextView.setText(mAuth.getCurrentUser().getEmail());

//        button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                upload(imagePath);
//            }
//        });
        remoteConfig();
    }

    private void remoteConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        /*디버깅 테스트를 위해 사용, 릴리즈할 때에는 지울것*/
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);

        /*서버에 매칭되는 값이 없을 때 이쪽을 참조*/
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);

        // cacheExpirationSeconds is set to cacheExpiration here, indicating that any previously
// fetched and cached config would be considered expired because it would have been fetched
// more than cacheExpiration seconds ago. Thus the next fetch would go to the server unless
// throttling is in progress. The default expiration duration is 43200 (12 hours).
        mFirebaseRemoteConfig.fetch(0)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(HomeActivity.this, "Fetch Succeeded",
                                    Toast.LENGTH_SHORT).show();

                            // Once the config is successfully fetched it must be activated before newly fetched
                            // values are returned.
                            mFirebaseRemoteConfig.activateFetched();
                        } else {
                            Toast.makeText(HomeActivity.this, "Fetch Failed",
                                    Toast.LENGTH_SHORT).show();
                        }
                        displayWelcomeMessage();
                    }
                });

    }

    private void displayWelcomeMessage() {
        String toolbarColor = mFirebaseRemoteConfig.getString("toolBarColor");
        Boolean aBoolean = mFirebaseRemoteConfig.getBoolean("welcome_message_caps");
        String message = mFirebaseRemoteConfig.getString("welcome_message");

        toolbar.setBackgroundColor(Color.parseColor(toolbarColor));

        if(aBoolean){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(message).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    HomeActivity.this.finish();
                }
            });
            builder.create().show();
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_Board) {

            Intent intent = new Intent(this,BoardActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_gallery) {

//            Intent intent = new Intent(Intent.ACTION_PICK);
//            intent.setType(MediaStore.Images.Media.CONTENT_TYPE);
//            startActivityForResult(intent, GALLERY_CODE);
            Intent intent = new Intent(this,BoardActivity2.class);
            startActivity(intent);

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            LoginManager.getInstance().logOut();
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        if (requestCode == GALLERY_CODE) {
//
//        imagePath = getPath(data.getData());
//        File f = new File(imagePath);
//            imageView.setImageURI(Uri.fromFile(f));
//
//        }
//
//    }

//    public String getPath(Uri uri){
//        String [] proj = { MediaStore.Images.Media.DATA };
//        CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
//
//        Cursor cursor = cursorLoader.loadInBackground ();
//        int index = cursor.getColumnIndexOrThrow (MediaStore.Images.Media.DATA);
//
//        cursor.moveToFirst();
//
//        return cursor.getString(index);
//    }

//    private void upload(String uri) {
//        StorageReference storageRef = storage.getReference();
//
//        final Uri file = Uri.fromFile(new File(uri));
//        StorageReference riversRef = storageRef.child("images/"+file.getLastPathSegment());
//        UploadTask uploadTask = riversRef.putFile(file);
//
//        // Register observers to listen for when the download is done or if it fails
//        uploadTask.addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                // Handle unsuccessful uploads
//            }
//        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//            @Override
//            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
//                @SuppressWarnings("VisibleForTests")
//                Uri downloadUrl = taskSnapshot.getDownloadUrl();
//
//                ImageDTO imageDTO = new ImageDTO();
//                imageDTO.imageUrl = downloadUrl.toString();
//                imageDTO.title = title.getText().toString();
//                imageDTO.description = description.getText().toString();
//                imageDTO.uid = mAuth.getCurrentUser().getUid();
//                imageDTO.userId = mAuth.getCurrentUser().getEmail();
//                imageDTO.imageName = file.getLastPathSegment();
//
//                database.getReference().child("images").push().setValue(imageDTO);
//            }
//        });
//    }

//    public static class PlaceholderFragment extends Fragment {
//        /**
//         * The fragment argument representing the section number for this
//         * fragment.
//         */
//        private static final String ARG_SECTION_NUMBER = "section_number";
//
//        public PlaceholderFragment() {
//        }
//
//        /**
//         * Returns a new instance of this fragment for the given section
//         * number.
//         */
//        public static PlaceholderFragment newInstance(int sectionNumber) {
//            PlaceholderFragment fragment = new PlaceholderFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.tab1abs, container, false);
//            return rootView;
//        }
//    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            //return PlaceholderFragment.newInstance(position + 1);
            switch (position) {
                case 0:
                    Tab1Abs tab1 = new Tab1Abs();
                    return tab1;
                case 1:
                    Tab2Attor tab2 = new Tab2Attor();
                    return tab2;
                case 2:
                    Tab3Etc tab3 = new Tab3Etc();
                    return tab3;
                default:
                    return null;

            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Main Ads";
                case 1:
                    return "Attorney prof.";
                case 2:
                    return "etc";
            }
            return null;
        }
    }

}
