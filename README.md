sequence-view
=============

An android lib for showing sequence images with management on RAM cache(yes or no).

features
=============
This is a image sequence view which could show sequenced big images like video. It is helpful when we wants to show a 2.5D sence.

We know android provides a limited RAM size for each app running in java VM. This limits the sequence view images performance. If we could load all sequence images at the same time, the view will be more fluent.

This lib helps make use of all device RAM avaible. We modified the jni image cache lib from https://github.com/AndroidDeveloperLB/AndroidJniBitmapOperations, which provides a jni ram cache for android images.

bugs know
=============
We don't provide a RAM size inspect, so be careful when using this lib.

Sample
=============
We now don't provide android style parameters. We use the classic Java style parameter.

	// Set a background image to sequence view, this is not required.
	// If you set, the sequence view size will depends on the background.
	seq.setBackgroundUrl(Environment.getExternalStorageDirectory()
				+ "/Pictures/building/others/location_01.jpg");
	// Set the sequence images. The string pattern and the length.(from 0 to the number)
	seq.setPath("/Pictures/building/others/roadnet%03d.png", 29);
	// Set the slide direction, right or left.
	seq.setDirection(1);
	// Set slide enabled. Default true.
	seq.setEnabled(true);
	// Set auto play sequence view.
	seq.setAutoPlay(true);
	// Set use cache or not, default false;
	seq.setCache(true);
	
	// Remember to call this after initialization.
	seq.prepare();

Contacts
============
Contact me at: yazhang008@gmail.com
