package emy.partners.lawapp

import coil3.ImageLoader
import coil3.disk.DiskCache
import coil3.memory.MemoryCache
import coil3.request.CachePolicy
import coil3.util.DebugLogger

//class MyApplication: Application(), ImageLoaderFactory {
//
//    override fun newImageLoader(): ImageLoader {
//        return ImageLoader(this).newBuilder()
//            .memoryCachePolicy(CachePolicy.ENABLED)
//            .memoryCache {
//                MemoryCache.Builder(this)
//                    .maxSizePercent(0.1)
//                    .strongReferencesEnabled(true)
//                    .build()
//            }
//            .diskCachePolicy(CachePolicy.ENABLED)
//            .diskCache {
//                DiskCache.Builder()
//                    .maxSizePercent(0.03)
//                    .directory(cacheDir)
//                    .build()
//            }
//            .logger(DebugLogger())
//            .build()
//    }
//}