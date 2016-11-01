package com.daemo.myfirstapp.connectivity.volley;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.daemo.myfirstapp.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class VolleyFragment extends Fragment implements View.OnClickListener {


    private TextView mTextView;
    private final String StringRequestTag = "StringRequestTag";
    private final String imageURL = "http://i.imgur.com/7spzG.png";
    private RequestQueue mRequestQueue;

    public VolleyFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_volley, container, false);
        mTextView = (TextView) root.findViewById(R.id.tv_volley);
        root.findViewById(R.id.string_req_btn).setOnClickListener(this);
        root.findViewById(R.id.image_req_btn).setOnClickListener(this);
        root.findViewById(R.id.image_req_l_btn).setOnClickListener(this);
        root.findViewById(R.id.image_req_n_btn).setOnClickListener(this);
        root.findViewById(R.id.clear_btn).setOnClickListener(this);
        return root;
    }

    public void createStringRequest() {
        // Instantiate the cache
        Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 1024 * 1024); // 1MB cap

// Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Get a RequestQueue
        mRequestQueue = VolleySingleton.getInstance(getContext()).
                getRequestQueue();

// Instantiate the RequestQueue with the cache and network.
        mRequestQueue = new RequestQueue(cache, network);

// Start the queue
        mRequestQueue.start();

        String url = "http://www.google.com";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        mTextView.setText("Response is: " + response.substring(0, 500));
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mTextView.setText("That didn't work!");
            }
        });

        stringRequest.setTag(StringRequestTag);
// Add the request to the RequestQueue.
        VolleySingleton.getInstance(getContext().getApplicationContext()).addToRequestQueue(stringRequest);
    }

    public void createImageRequest() {
        final ImageView mImageView;
        mImageView = (ImageView) getView().findViewById(R.id.iv_volley);

// Retrieves an image specified by the URL, displays it in the UI.
        ImageRequest request = new ImageRequest(imageURL,
                new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap bitmap) {
                        mImageView.setImageBitmap(bitmap);
                    }
                }, 0, 0, ImageView.ScaleType.CENTER_INSIDE, null,
                new Response.ErrorListener() {
                    public void onErrorResponse(VolleyError error) {
                        mImageView.setImageResource(R.drawable.empty_photo);
                    }
                });
// Access the RequestQueue through your singleton class.
        VolleySingleton.getInstance(getContext().getApplicationContext()).addToRequestQueue(request);
    }

    public void createImageRequestWithLoader() {
        ImageView mImageView = (ImageView) getView().findViewById(R.id.iv_volley);

        // Get the ImageLoader through your singleton class.
        ImageLoader mImageLoader = VolleySingleton.getInstance(getContext().getApplicationContext()).getImageLoader();
        mImageLoader.get(imageURL, ImageLoader.getImageListener(mImageView,
                R.drawable.empty_photo, R.drawable.empty_photo));
    }

    public void createImageRequestWithNetwork() {
        // Get the NetworkImageView that will display the image.
        NetworkImageView mNetworkImageView = (NetworkImageView) getView().findViewById(R.id.niv_volley);

// Get the ImageLoader through your singleton class.
        ImageLoader mImageLoader = VolleySingleton.getInstance(getContext().getApplicationContext()).getImageLoader();

// Set the URL of the image that should be loaded into this view, and
// specify the ImageLoader that will be used to make the request.
        mNetworkImageView.setImageUrl(imageURL, mImageLoader);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(StringRequestTag);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.string_req_btn:
                createStringRequest();
                break;
            case R.id.image_req_btn:
                createImageRequest();
                break;
            case R.id.image_req_l_btn:
                createImageRequestWithLoader();
                break;
            case R.id.image_req_n_btn:
                createImageRequestWithNetwork();
                break;
            case R.id.clear_btn:
                ((ImageView) getView().findViewById(R.id.iv_volley)).setImageResource(R.drawable.empty_photo);
                ((NetworkImageView) getView().findViewById(R.id.niv_volley)).setImageUrl(null, null);
        }
    }
}
