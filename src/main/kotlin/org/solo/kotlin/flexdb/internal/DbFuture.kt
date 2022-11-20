package org.solo.kotlin.flexdb.internal

import org.solo.kotlin.flexdb.GlobalData
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor

class DbFuture<T> : CompletableFuture<T>() {
    override fun defaultExecutor(): Executor {
        return GlobalData.cachedExecutor
    }

    companion object {
        @JvmStatic
        fun doRunAsync(runnable: Runnable): CompletableFuture<Void> {
            return runAsync(runnable, GlobalData.cachedExecutor)
        }
    }
}