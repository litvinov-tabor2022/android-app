package cz.jenda.tabor2022.adapters

interface WithItemListeners<T> {
    fun setOnItemClickListener(listener: OnItemShortClickListener<T>)
    fun setOnLongItemClickListener(listener: OnItemLongClickListener<T>)
}

interface OnItemShortClickListener<T> {
    fun itemShortClicked(item: T)
}

interface OnItemLongClickListener<T> {
    fun itemLongClicked(item: T)
}