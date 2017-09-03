package com.github.premnirmal.ticker.components

import android.content.Context
import timber.log.Timber

/**
 * Created by premnirmal on 2/28/16.
 */
class LoggingTree : Timber.DebugTree {

  constructor(context: Context) {
    Timber.plant(this)
  }
}