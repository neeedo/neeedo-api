package common.helper

import java.io.FileNotFoundException

import org.specs2.mutable.Specification
import test.TestApplications

class FileHelperSpec extends Specification {
  "FileHelper" should {
    "string from existing file must return correct string" in TestApplications.loggingOffApp() {
      FileHelper.stringFromFile("common/helper/file.txt") mustEqual "test"
    }

    "string from nonexisting file must throw FileNotFound" in TestApplications.loggingOffApp() {
      FileHelper.stringFromFile("common/helper/file1.txt") must throwA(new FileNotFoundException("common/helper/file1.txt"))
    }
  }
}
