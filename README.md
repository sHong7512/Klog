# Klog

A "custom log class" to help you develop For Android.

With floating log window updated with key values.

## Get Start

- dependencies for download jitpack

```
// settings.gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

- dependencies for library

```
// buld.gradle(:app)
    implementation 'com.github.sHong7512:AARLibraryExample:0.0.2'
```

- Call initalize() if you want to set "searchPoint" and "isShow"

```
    Klog.initialize("_sHong", BuildConfig.DEBUG)
```

### Basic Logcat with SearchPoint
   
- Show Logcat 
```
    KLog.d("your tag", "your message")
```

```
 // Klog.kt
 fun d(any: Any, msg: String)
 fun d(any: Any, msg: String, thr: Throwable?)
 fun d(tag: String, msg: String)
 fun d(tag: String, msg: String, thr: Throwable?)
```

- Other options(V, D, I, W, E) are the same

### Floating Log

- Put the code in your activity (This function includes an permission request)

```
    Klog.runFloating(activity)
```

- If you want permission

```
     Klog.reqPermission(context)
```

- If you want permission With ActivityResultLauncher

```
     Klog.reqPermissionWithLauncher(
            componentActivity,
            {
                ...
                Klog.runFloating(activity)
            },
            { ... },
        )
```

- If you want to close when you press the Back button on the "BaseActivity", insert this cord

```
    Klog.addBackPressedFloatingClose(componentActivity)
```

- If you want to stop

```
    Klog.stopFloating(acivity)
```

- Show Log at FLoating Window

```
    Klog.f("your tag", "your message")
    Klog.fl("your tag", "your message", LogLevel.E)
```

```
 // Klog.kt
 fun f(any: Any, msg: String)
 fun f(any: Any, msg: String, thr: Throwable?)
 fun f(tag: String, msg: String)
 fun f(tag: String, msg: String, thr: Throwable?)
 
 fun fl(any: Any, msg: String)
 fun fl(any: Any, msg: String, thr: Throwable?)
 fun fl(tag: String, msg: String)
 fun fl(tag: String, msg: String, thr: Throwable?)
```

### Floating other Options

```
fun runFloating(
        activity: activity,
        autoStop: Boolean = AUTO_STOP_BASE,
        max: Int = MAX_BASE,
        withActivityLog: Boolean = false,
        onFailure: ((String?) -> Unit)?,
    ) 
```

- activity : ComponentActivity,

- autoStop : Automatic shutdown when no activity(task) is active

- max : Floating Log lines

- withActivityLog :  Show TaskInfo(TopActivity, BaseActivity, Activities)

- onPermissionOk : Called when permission check is complete

- onFailure : Called when runfloating is failure

## Errors Handle

- sdk < 23

```
// AndroidManifest
<Manifest ...>
    <uses-sdk tools:overrideLibrary="com.shong.klog"/>
...
</Manifest>
```

- java.lang.NoClassDefFoundError: Failed resolution of: Landroidx/activity/result/contract/ActivityResultContracts$StartActivityForResult;

```
// buld.gradle(:app)
    implementation "androidx.activity:activity:1.2.0"
    implementation "androidx.fragment:fragment:1.3.0"
```

## License
```
Copyright 2023, sHong

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
