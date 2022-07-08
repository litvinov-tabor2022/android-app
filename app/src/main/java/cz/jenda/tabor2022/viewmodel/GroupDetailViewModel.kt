package cz.jenda.tabor2022.viewmodel

import androidx.databinding.BaseObservable
import cz.jenda.tabor2022.data.model.GroupStatistics
import cz.jenda.tabor2022.data.model.User

class GroupDetailViewModel(val users: List<User>, val statistics: GroupStatistics) :
    BaseObservable()