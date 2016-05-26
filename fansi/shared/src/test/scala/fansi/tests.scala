package test.fansi

import utest._


object FansiTests extends TestSuite{

  // Alias a bunch of rendered attributes to short names
  // to use in all our test cases
  val R = fansi.Color.Red.escape
  val G = fansi.Color.Green.escape
  val B = fansi.Color.Blue.escape
  val Y = fansi.Color.Yellow.escape
  val UND = fansi.Underlined.On.escape
  val DUND = fansi.Underlined.Off.escape
  val REV = fansi.Reversed.On.escape
  val DREV = fansi.Reversed.Off.escape
  val DCOL = fansi.Color.Reset.escape
  val RES = fansi.Attr.Reset.escape
  /**
    * ANSI escape sequence to reset text color
    */
  val RTC = fansi.Color.Reset.escape
  val tests = TestSuite{
    val rgbOps = s"+++$R---$G***$B///"
    val rgb = s"$R$G$B"
    'parsing{
      val r = fansi.Str(rgbOps).render
      assert(
        fansi.Str(rgbOps).plainText == "+++---***///",
        fansi.Str(rgb).plainText == "",
        r == rgbOps + RTC,
        fansi.Str(rgb).render == ""
      )
    }

    'equality{
      assert(fansi.Color.Red("foo") == fansi.Color.Red("foo"))
    }
    'concat{
      val concated = (fansi.Str(rgbOps) ++ fansi.Str(rgbOps)).render
      val expected = rgbOps ++ RTC ++ rgbOps ++ RTC

      assert(concated == expected)
    }

    'split{
      val splits = Seq(
        // These are the standard series
        (0,  s"", s"+++$R---$G***$B///$RTC"),
        (1,  s"+", s"++$R---$G***$B///$RTC"),
        (2,  s"++", s"+$R---$G***$B///$RTC"),
        (3,  s"+++", s"$R---$G***$B///$RTC"),
        (4,  s"+++$R-$RTC", s"$R--$G***$B///$RTC"),
        (5,  s"+++$R--$RTC", s"$R-$G***$B///$RTC"),
        (6,  s"+++$R---$RTC", s"$G***$B///$RTC"),
        (7,  s"+++$R---$G*$RTC", s"$G**$B///$RTC"),
        (8,  s"+++$R---$G**$RTC", s"$G*$B///$RTC"),
        (9,  s"+++$R---$G***$RTC", s"$B///$RTC"),
        (10, s"+++$R---$G***$B/$RTC", s"$B//$RTC"),
        (11, s"+++$R---$G***$B//$RTC", s"$B/$RTC"),
        (12, s"+++$R---$G***$B///$RTC", s"")
      )
      for((index, expectedLeft0, expectedRight0) <- splits){
        val (splitLeft, splitRight) = fansi.Str(rgbOps).splitAt(index)
        val (expectedLeft, expectedRight) = (expectedLeft0, expectedRight0)
        val left = splitLeft.render
        val right = splitRight.render
        assert((left, right) == (expectedLeft, expectedRight))
      }
    }
    'substring{
      val substringed = fansi.Str(rgbOps).substring(4, 9).render
      assert(substringed == s"$R--$G***$RTC")
    }

    'overlay{
      'simple{
        val overlayed = fansi.Str(rgbOps).overlay(fansi.Color.Yellow, 4, 7)
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed.render == expected)
      }
      'resetty{
        val resetty = s"+$RES++$R--$RES-$RES$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render
        val expected = s"+++$R-$Y--*$G**$B///$RTC"
        assert(overlayed == expected)
      }
      'mixedResetUnderline{
        val resetty = s"+$RES++$R--$RES-$UND$G***$B///"
        val overlayed = fansi.Str(resetty).overlay(fansi.Color.Yellow, 4, 7).render toVector
        val expected = s"+++$R-$Y--$UND*$G**$B///$DCOL$DUND" toVector

        assert(overlayed == expected)
      }
      'underlines{
        val resetty = s"$UND#$RES    $UND#$RES"
        'underlineBug{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 2).render
          val expected = s"$UND$REV#$DUND $DREV   $UND#$DUND"
          assert(overlayed == expected)
        }
        'barelyOverlapping{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 1).render
          val expected = s"$UND$REV#$DUND$DREV    $UND#$DUND"
          assert(overlayed == expected)
        }
        'endOfLine{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 6).render
          val expected = s"$UND#$DUND    $UND$REV#$DUND$DREV"
          assert(overlayed == expected)
        }
        'overshoot{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 5, 10).render.toVector
          val expected = s"$UND#$DUND    $UND$REV#$DUND$DREV".toVector
          assert(overlayed == expected)
        }
        'empty{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 0, 0).render
          val expected = s"$UND#$DUND    $UND#$DUND"
          assert(overlayed == expected)
        }
        'singleContent{
          val overlayed = fansi.Str(resetty).overlay(fansi.Reversed.On, 2, 4).render
          val expected = s"$UND#$DUND $REV  $DREV $UND#$DUND"
          assert(overlayed == expected)

        }
      }
    }
    'attributes{
      * - {
        Console.RESET + fansi.Underlined.On
      }
      * - {
        Console.RESET + (fansi.Underlined.On("Reset ") ++ fansi.Underlined.Off("Underlined"))
      }
      * - {
        Console.RESET + fansi.Bold.On
      }
      * - {
        Console.RESET + (fansi.Bold.On("Reset ") ++ fansi.Bold.Off("Bold"))
      }
      * - {
        Console.RESET + fansi.Reversed.On
      }
      * - {
        Console.RESET + (fansi.Reversed.On("Reset ") ++ fansi.Reversed.Off("Reversed"))
      }
    }
    def tabulate(all: Seq[fansi.Attr]) = {
      all.map(attr => attr.toString + " " * (25 - attr.name.length))
         .grouped(3)
         .map(_.mkString)
         .mkString("\n")
    }

    'colors - tabulate(fansi.Color.all)
    'backgrounds - tabulate(fansi.Back.all)
    'negative{
      'parse{
        // Make sure that fansi.Str throws on most common non-color
        // fansi terminal commands
        //
        // List of common non-color fansi terminal commands taken from
        // https://en.wikipedia.org/wiki/ANSI_escape_code#Non-CSI_codes

        def check(s: String, msg: String) ={
          intercept[IllegalArgumentException]{ fansi.Str(s, strict = true) }
  //        assert(ex.getMessage.contains(msg))
        }

        'cursorUp - check("Hello\u001b[2AWorld", "[2A")
        'cursorDown- check("Hello\u001b[2BWorld", "[2B")
        'cursorForward - check("Hello\u001b[2CWorld", "[2C")
        'cursorBack - check("Hello\u001b[2DWorld", "[2D")
        'cursorNextLine - check("Hello\u001b[2EWorld", "[2E")
        'cursorPrevLine - check("Hello\u001b[2FWorld", "[2F")
        'cursorHorizontalAbs - check("Hello\u001b[2GmWorld", "[2G")
        'cursorPosition- check("Hello\u001b[2;2HmWorld", "[2;2H")
        'eraseDisplay - check("Hello\u001b[2JWorld", "[2J")
        'eraseLine - check("Hello\u001b[2KWorld", "[2K")
        'scrollUp - check("Hello\u001b[2SWorld", "[2S")
        'scrollDown - check("Hello\u001b[2TWorld", "[2T")
        'horizontalVerticalPos - check("Hello\u001b[2;2fWorld", "[2;2f")
        'selectGraphicRendition - check("Hello\u001b[2mWorld", "[2m")
        'auxPortOn - check("Hello\u001b[5iWorld", "[5i")
        'auxPortOff - check("Hello\u001b[4iWorld", "[4i")
        'deviceStatusReport - check("Hello\u001b[6n", "[6n")
        'saveCursor - check("Hello\u001b[s", "[s")
        'restoreCursor - check("Hello\u001b[u", "[u")

      }
      'outOfBounds{
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(10) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(4) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").splitAt(-1) }
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(0, 4)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(-1, 2)}
        intercept[IllegalArgumentException]{ fansi.Str("foo").substring(2, 1)}
      }
    }
    'multipleAttrs{
      'identicalMasksGetCollapsed{
        val redRed = fansi.Color.Red ++ fansi.Color.Red
        assert(
          redRed.resetMask == fansi.Color.Red.resetMask,
          redRed.applyMask == fansi.Color.Red.applyMask
        )
      }
      'overlappingMasksGetReplaced{
        val redBlue = fansi.Color.Red ++ fansi.Color.Blue
        assert(
          redBlue.resetMask == fansi.Color.Blue.resetMask,
          redBlue.applyMask == fansi.Color.Blue.applyMask
        )
      }
      'semiOverlappingMasks{
        val resetRed = fansi.Attr.Reset ++ fansi.Color.Red
        val redReset = fansi.Color.Red ++ fansi.Attr.Reset
        assert(
          resetRed != fansi.Attr.Reset,
          resetRed != fansi.Color.Red,
          redReset == fansi.Attr.Reset,
          redReset != fansi.Color.Red,
          redReset != resetRed,
          resetRed.resetMask == fansi.Attr.Reset.resetMask,
          resetRed.applyMask == fansi.Color.Red.applyMask
        )
      }
      'separateMasksGetCombined{
        val redBold = fansi.Color.Red ++ fansi.Bold.On

        assert(
          redBold.resetMask == (fansi.Color.Red.resetMask | fansi.Bold.On.resetMask),
          redBold.applyMask == (fansi.Color.Red.applyMask | fansi.Bold.On.applyMask)
        )
      }
      'applicationWorks{
        val redBlueBold = fansi.Color.Red ++ fansi.Color.Blue ++ fansi.Bold.On
        val colored = redBlueBold("Hello World")
        val separatelyColored = fansi.Bold.On(fansi.Color.Blue(fansi.Color.Red("Hello World")))
        assert(colored.render == separatelyColored.render)
      }
      'equality{
        assert(
          fansi.Color.Blue ++ fansi.Color.Red == fansi.Color.Red,
          fansi.Color.Red == fansi.Color.Blue ++ fansi.Color.Red,
          fansi.Bold.On ++ fansi.Color.Red != fansi.Color.Red,
          fansi.Color.Red != fansi.Bold.On ++ fansi.Color.Red
        )
      }
    }
//    'perf{
//      val input = s"+++$R---$G***$B///" * 1000
//
//      'parsing{
//
//        val start = System.currentTimeMillis()
//        var count = 0
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansi.Str(input)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'concat{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansiStr ++ fansiStr
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'splitAt{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          fansiStr.splitAt(count % fansiStr.length)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'substring{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          val start = count % fansiStr.length
//          val end = count % (fansiStr.length - start) + start
//          fansiStr.substring(start, end)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//      'overlay{
//        val start = System.currentTimeMillis()
//        var count = 0
//        val fansiStr = fansi.Str(input)
//        val attrs =
//          fansi.Color.Red ++
//          fansi.Color.Blue ++
//          fansi.Bold.On ++
//          fansi.Reversed.On ++
//          fansi.Bold.Off ++
//          fansi.Underlined.On
//
//        while(System.currentTimeMillis() < start + 5000){
//          count += 1
//          val start = count % fansiStr.length
//          val end = count % (fansiStr.length - start) + start
//          fansiStr.overlay(attrs, start, end)
//        }
//        val end = System.currentTimeMillis()
//        count
//      }
//    }
  }
}

