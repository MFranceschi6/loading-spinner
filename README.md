# Android Like Spinner with Jetpack Compose 🚀

[![](https://jitpack.io/v/MFranceschi6/loading-spinner.svg)](https://jitpack.io/#MFranceschi6/loading-spinner) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)




This is a simple implementation of the common loading spinner of Android implemented as a `Compose Modifier` using the Jetpack Compose Animations.

Compatible with Compose Version **0.1.0-dev15**
## Example in action

<img src="https://user-images.githubusercontent.com/9447889/88922960-3628c380-d271-11ea-8ef8-825bbb69c335.gif" width="300"/>

## Example with code

```kotlin
@Composable
fun mainView() {
  var loading by state { true }
  
  launchInComposition {
    while(true){
      delay(5000)
      loading = !loading
    }
  }
  
  Column(modifier = Modifier.fillMaxSize().drawBackground(Color.Yellow).loadingSpinner(loading).drawBackground(Color.Cyan), horizontalGravity = Alignment.CenterHorizontally) {
    Surface(
      modifier = Modifier.drawBackground(Color.Red).loadingSpinner(
        loading = !loading,
        size = SpinnerSize.FitContainer
      ).fillMaxWidth().height(400.dp)) { }
    Surface(modifier = Modifier.preferredSize(190.dp, 190.dp).drawBackground(Color.Green).loadingSpinner(loading = !loading, color = Color.Red)) { }
  }
}
```

## Download

Available through jitpack.

Add the maven repo to your root `build.gradle`

```gradle
allprojects {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```


Add the dependency

```gradle
  implementation 'com.github.MFranceschi6:loading-spinner:0.15.0'
```

## How To Use

This repo exposes the function `Modifier.loadingSpinner` which it returns a Modifier which shows the loading spinner.

### loadingSpinner

`loadingSpinner` accepts the following arguments
* `loading: Boolean` if true shows the spinner, otherwise shows the contents of the element
* `color: Color? = null` the color for the spinner, otherwise it uses `MaterialTheme.colors.primary`
* `width: Float = 16F` the size of the `Stroke` used to draw the spinner
* `size: Size = SpinnerSize.Medium` the desired size for the spinner

if you need to apply modifiers which draw something on the element it's important to use them before the `loadingSpinner` modifier otherwise they won't be placed
when the spinner is showing:

```kotlin
Column(modifier = Modifier.drawBackground(Color.Red).loadingSpinner(loading = isLoading, color = Color.Green).drawBackground(Color.Blue)){
  Text(text = "Hello Compose 🚀")
}

```
If `isLoading` is `true` the column will show a red background with a green spinner, otherwise it will show a blue background with the `Text` element

<img src="https://user-images.githubusercontent.com/9447889/88927734-2791da80-d278-11ea-8cd9-04052496e875.gif" width="300"/>

### SpinnerSize

Helper function and object which provides standard spinner sizes.
It also provides `SpinnerSize.FillElement` which if passed as the size of the spinner will cause the spinner to fill the available space of the element to which is applied
