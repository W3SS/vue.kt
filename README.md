# vue.kt

This is very much an alpha / proof-of-concept. This library allows you to use Vue from Kotlin - in a type-safe way!

For an example, check out [vue.kt-test](https://github.com/frozenice/vue.kt) - here is a snippet:

```kotlin
class AppData(val name: String, val todos: Array<String>) : VueData

object App1 : ComponentOptions<AppData, VueProps>() {
  override val el: String?
    get() = "#app1"

  override val template: (TagConsumer<String>.() -> Unit)?
    get() = {
      div {
        +"Hello {{name}}"
        component(App1Sub, SubProps(num = 23))
        component(App1Sub, SubProps(num = 42))
      }
    }

  override val data: AppData?
    get() = AppData(name = "tester")
}

class SubProps(val num: Number) : VueProps

object App1Sub : ComponentOptions<VueData, SubProps>() {
  override val template: (TagConsumer<String>.() -> Unit)?
    // ...
}

```
