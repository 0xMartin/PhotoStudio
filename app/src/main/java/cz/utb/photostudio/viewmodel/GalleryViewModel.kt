package cz.utb.photostudio.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

enum class GalleryStatus { LOADING, ERROR, DONE }

class GalleryViewModel : ViewModel() {

    private val _status = MutableLiveData<GalleryStatus>()

    val status: LiveData<GalleryStatus>
        get() = _status

    private val _properties = MutableLiveData<List<Object>>()

    val properties: LiveData<List<Object>>
        get() = _properties

}