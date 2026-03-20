package com.example.kmptemplate.shared.testing

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.Settings

class FakeSettings : Settings by MapSettings()
