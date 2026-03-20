package com.example.kmptemplate

import com.example.kmptemplate.shared.uimodel.di.MockAppComponent

// UI テストでは MockAppComponent を注入して実際の API を呼ばない
val fakeComponent = MockAppComponent()
