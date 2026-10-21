package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.StepRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("SMARTSTEP", appName)
  }

  @Test
  fun `verify step calculations and metrics without calories`() {
    val steps = 6000
    val targetSteps = 6000
    // Rumus: 1 langkah ~ 0.75 meter
    val distanceMeters = steps * 0.75
    val percentage = (steps.toFloat() / targetSteps.toFloat()) * 100f

    assertEquals(4500.0, distanceMeters, 0.01)
    assertEquals(100f, percentage, 0.01f)

    val record = StepRecord(
      dateString = "21 Okt 2026",
      timeString = "07:00 - 08:00 WIB",
      timestamp = System.currentTimeMillis(),
      steps = steps,
      distanceMeters = distanceMeters,
      durationSeconds = 3600L
    )

    assertEquals(6000, record.steps)
    assertEquals(4500.0, record.distanceMeters, 0.01)
    assertEquals(3600L, record.durationSeconds)
  }
}
