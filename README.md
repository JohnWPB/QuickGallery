# QuickGallery 0.1 prototype
Android 16-oriented folder gallery proof of concept. MediaStore folder list, exclusion toggles, thumbnail grid, basic image navigation and VideoView swipe seeking.

## Cloud build
Upload all project files to a GitHub repository (including the hidden `.github/workflows/android.yml` path). GitHub Actions builds a debug-signed APK and uploads it as `QuickGallery-debug-apk` under the workflow run's Artifacts.

## Known limitations
This is a prototype, not a feature-complete QuickPic replacement. Manual directory inclusion via Storage Access Framework, pinch zoom, proper continuous frame-accurate video scrubbing, advanced video controls, and thumbnail caching are not implemented. VideoView seeking is keyframe-dependent. MediaStore discovery only sees indexed files. Android's selected-photo access may limit results. The app has not been compiled or tested on a physical device.
