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
