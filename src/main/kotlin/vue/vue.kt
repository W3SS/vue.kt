/*
 * Copyright 2018 project contributors (see CONTRIBUTORS file)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vue

import kotlinx.html.HTMLTag
import kotlinx.html.TagConsumer
import kotlinx.html.stream.createHTML
import org.w3c.dom.HTMLElement
import kotlin.collections.set

@JsModule("vue/dist/vue.common.js")
@JsName("Vue")
@JsNonModule
open external class Vue<D : VueData, P : VueProps>(options: VueOptions<D, P> = definedExternally) {
  /**
   * [see Vue docs](https://vuejs.org/v2/api/#Global-Config)
   */
  object config {
    /**
     * Suppress all Vue logs and warnings.
     * [see Vue docs](https://vuejs.org/v2/api/#silent)
     */
    var silent: Boolean
    // TODO
  }

  companion object {

    // https://vuejs.org/v2/api/#Global-API
    // TODO
    fun <D : VueData, P : VueProps> component(id: String, options: VueOptions<D, P>)
    // TODO
  }
}

abstract external class VueInstance<D : VueData, P : VueProps> {
  // https://vuejs.org/v2/api/#Instance-Properties
  val `$data`: D?
  val `$props`: P?
  val `$el`: HTMLElement
  val `$options`: VueOptions<D, P>
  val `$parent`: VueInstance<*, *>
  val `$root`: VueInstance<*, *>
  val `$children`: Array<VueInstance<*, *>>
  // TODO

  // https://vuejs.org/v2/api/#Instance-Methods-Data
  // TODO

  // https://vuejs.org/v2/api/#Instance-Methods-Events
  // TODO

  // https://vuejs.org/v2/api/#Instance-Methods-Lifecycle
  // TODO
}

//typealias AsyncComponentLoader = () -> Promise<JsClass<Component<*, *>>>

interface VueData

interface VueProps

class VueOptions<D : VueData, P : VueProps> {
  var el: String? = null
  var template: String? = null
  var data: D? = null
  var props: Array<String>? = null
  var propsData: Map<String, Any?>? = null
  var components: Map<String, Any>? = null
}

open class ComponentOptions<D : VueData, P : VueProps> {
  private fun toVueOptions(forComponent: Boolean = false): VueOptions<D, P> = VueOptions<D, P>().also { opts ->
    if (!forComponent) opts.el = el

    template?.let {
      val htmlBuilder = createHTML()
      it(htmlBuilder)
      opts.template = htmlBuilder.finalize()
    }

    opts.data = data ?: { Object.create(null).asDynamic() }.asDynamic()

    props?.let {
      val _props = mutableListOf<String>()
      val _propsData = mutableMapOf<String, Any?>()

      for (key in Object.keys(it)) {
        _props.add(key)
        _propsData[key] = it.asDynamic()[key]
      }

      opts.props = _props.toTypedArray()
      if (!forComponent) opts.propsData = _propsData.toJsObject() // TODO make available to dedupe code below
    }

    components.let {
      if (it.isNotEmpty()) opts.components = it.mapValues { it.value.toVueOptions(true) }.toJsObject()
    }
  }.toJsObject(true)

  open val el: String? = null

  open val template: (TagConsumer<String>.() -> Unit)? = null

  private val components: MutableMap<String, ComponentOptions<*, *>> = mutableMapOf()
  private val componentCtorNames: MutableMap<JsClass<*>, String> = mutableMapOf()
  fun <Csub : ComponentOptions<*, Psub>, Psub : VueProps> TagConsumer<String>.component(
    component: Csub,
    props: Psub? = null
  ) {
    val ctor = component::class.js
    var compName = ctor.name

    if (!componentCtorNames.containsKey(ctor)) {
      componentCtorNames[ctor] = compName
    } else if (components[compName] != component) {
      compName = "${compName}${components.size}"
    }

    component.props = props
    components[compName] = component

    val _propsData = mutableMapOf<String, String>()
    if (props != null) {
      for (key in Object.keys(props)) {
        val value = props.asDynamic()[key]
        if (value != null) _propsData[key] = value.toString()
      }
    }

    val tag = object : HTMLTag(compName, this, _propsData, inlineTag = true, emptyTag = false) {}
    onTagStart(tag)
    // TODO sub-builder for slots
    onTagEnd(tag)
  }

  open val data: D? = null

  private var props: P? = null

  private var vm: VueInstance<D, P>? = null

  /**
   * Instantiates a new VM with these options.
   */
  fun render(): VueInstance<D, P> {
    if (vm == null) {
      vm = Vue(toVueOptions()).asDynamic()
    }
    return vm!!
  }
}
