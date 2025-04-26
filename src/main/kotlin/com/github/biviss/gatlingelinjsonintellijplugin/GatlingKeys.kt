package com.github.biviss.gatlingelinjsonintellijplugin

import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.util.Key

object GatlingKeys {
    val EL_MARKERS_KEY = Key.create<MutableList<RangeMarker>>("GATLING_EL_MARKERS")
}