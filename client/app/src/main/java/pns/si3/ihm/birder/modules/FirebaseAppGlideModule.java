package pns.si3.ihm.birder.modules;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

/**
 * Firebase glide module.
 *
 * Allows the application to load images from firebase storage.
 * Glide manages a cache to avoid requesting the same images multiple times.
 */
@GlideModule
public class FirebaseAppGlideModule extends AppGlideModule {
	@Override
	public void registerComponents(Context context, Glide glide, Registry registry) {
		// Register the firebase image loader.
		registry.append(
			StorageReference.class,
			InputStream.class,
			new FirebaseImageLoader.Factory()
		);
	}
}
