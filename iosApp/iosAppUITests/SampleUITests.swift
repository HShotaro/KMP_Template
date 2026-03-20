import XCTest

final class SampleUITests: XCTestCase {
    var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testLoginScreenIsVisible() {
        XCTAssertTrue(app.staticTexts["KMP Template"].exists)
    }
}
