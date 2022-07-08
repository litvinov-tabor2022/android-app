package cz.jenda.tabor2022

import androidx.lifecycle.MutableLiveData

@Suppress("UNCHECKED_CAST")
class SafeMutableLiveData<T: Any>(private val mutableLiveData: MutableLiveData<T>) :
    MutableLiveData<T>(mutableLiveData.value) {

    override fun getValue(): T = mutableLiveData.value as T
    override fun setValue(value: T) {
        super.setValue(value)
        mutableLiveData.value = value
    }
    override fun postValue(value: T) {
        super.postValue(value)
        mutableLiveData.postValue(value)
    }
}