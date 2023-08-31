package com.ivy

import com.ivy.common.androidtest.IvyAndroidTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test

@HiltAndroidTest
class DummyTest: IvyAndroidTest() {

    @Test
    fun dummyTest() {
        // This is a dummy test to make sure that the test runner is working
        assert(true)
    }
}
