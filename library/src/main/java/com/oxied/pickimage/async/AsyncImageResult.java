package com.oxied.pickimage.async;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import com.oxied.pickimage.R;
import com.oxied.pickimage.bean.PickResult;
import com.oxied.pickimage.bundle.PickSetup;
import com.oxied.pickimage.enums.EPickType;
import com.oxied.pickimage.img.ImageHandler;
import com.oxied.pickimage.resolver.IntentResolver;

import java.lang.ref.WeakReference;

/**
 * Created by jrvansuita on 08/02/17.
 */

public class AsyncImageResult extends AsyncTask<Intent, Void, PickResult> {

    private WeakReference<IntentResolver> weakIntentResolver;
    private WeakReference<PickSetup> weakSetup;
    private OnFinish onFinish;

    private long startTime = System.currentTimeMillis();

    public AsyncImageResult(IntentResolver intentResolver, PickSetup setup) {
        this.weakIntentResolver = new WeakReference<>(intentResolver);
        this.weakSetup = new WeakReference<>(setup);
    }

    public AsyncImageResult setOnFinish(OnFinish onFinish) {
        this.onFinish = onFinish;
        return this;
    }

    @Override
    protected PickResult doInBackground(Intent... intents) {

        //Create a PickResult instance
        PickResult result = new PickResult();

        IntentResolver resolver = weakIntentResolver.get();

        if (resolver == null) {
            result.setError(new Error(resolver.getActivity().getString(R.string.activity_destroyed)));
            return result;
        }

        try {
            //Get the data intent from onActivityResult()
            Intent data = intents[0];

            //Define if it was pick from camera
            boolean fromCamera = resolver.fromCamera(data);

            //Instance of a helper class

            Uri originalUri = fromCamera ? resolver.cameraUri() : data.getData();
            Uri outputUri;
            if (weakSetup.get().getSaveDecodedToFile()) {
                outputUri = fromCamera ? originalUri : resolver.cameraUri();
            } else {
                outputUri = originalUri;
            }

            ImageHandler imageHandler = ImageHandler
                    .with(resolver.getActivity()).setup(weakSetup.get())
                    .provider(fromCamera ? EPickType.CAMERA : EPickType.GALLERY)
                    .uri(originalUri)
                    .outputUri(outputUri);

            //Setting uri and path for result
            result.setUri(imageHandler.getOutputUri())
                    .setPath(imageHandler.getOutputUriPath())
                    .setBitmap(imageHandler.decode());

            long passedTime = System.currentTimeMillis() - startTime;
            if (weakSetup.get().getMinLoadingTime() != 0
                    && passedTime < weakSetup.get().getMinLoadingTime()) {
                Thread.sleep(weakSetup.get().getMinLoadingTime() - passedTime);
            }

            return result;
        } catch (Exception e) {
            e.printStackTrace();
            result.setError(e);
            return result;
        }
    }


    @Override
    protected void onPostExecute(PickResult r) {
        if (onFinish != null)
            onFinish.onFinish(r);
    }

    public interface OnFinish {
        void onFinish(PickResult pickResult);
    }

}
