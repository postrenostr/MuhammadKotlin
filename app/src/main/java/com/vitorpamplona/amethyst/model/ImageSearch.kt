package com.vitorpamplona.amethyst.model

import java.io.Serializable

data class ImageSearch(var isImagesearch: Boolean = false, var searchKeyword: String? = null) :
    Serializable
