package cz.jenda.tabor2022.activities

import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

abstract class BasicActivity : AppCompatActivity(), CoroutineScope {

    protected var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job


}

