package scala.collection.parallel
package scalameter



import org.scalameter.api._



class RangeBench extends PerformanceTest.Regression with Serializable {
  import Par._
  import workstealing.WorkstealingTreeScheduler
  import workstealing.WorkstealingTreeScheduler.Config

  /* config */

  def persistor = new SerializationPersistor

  /* generators */

  val sizes = Gen.enumeration("size")(25000000, 50000000, 100000000, 150000000)
  val ranges = for (size <- sizes) yield 0 until size
  @transient lazy val s1 = new WorkstealingTreeScheduler.ForkJoin(new Config.Default(1))
  @transient lazy val s2 = new WorkstealingTreeScheduler.ForkJoin(new Config.Default(2))
  @transient lazy val s4 = new WorkstealingTreeScheduler.ForkJoin(new Config.Default(4))
  @transient lazy val s8 = new WorkstealingTreeScheduler.ForkJoin(new Config.Default(8))

  performance of "Par[Range]" in {

    measure method "fold" config(
      exec.minWarmupRuns -> 20,
      exec.maxWarmupRuns -> 50,
      exec.benchRuns -> 30,
      exec.independentSamples -> 6,
      exec.jvmflags -> "-XX:+UseCondCardMark"
    ) in {
      using(ranges) curve("Sequential") in { r =>
        var i = r.head
        val to = r.last
        var sum = 0
        while (i <= to) {
          sum += i
          i += 1
        }
        if (sum == 0) ???
      }

      using(ranges) curve("Par-1") in { r =>
        import workstealing.Ops._
        implicit val s = s1
        val pr = r.toPar
        pr.fold(0)(_ + _)
      }

      using(ranges) curve("Par-2") in { r =>
        import workstealing.Ops._
        implicit val s = s2
        val pr = r.toPar
        pr.fold(0)(_ + _)
      }

      using(ranges) curve("Par-4") in { r =>
        import workstealing.Ops._
        implicit val s = s4
        val pr = r.toPar
        pr.fold(0)(_ + _)
      }

      using(ranges) curve("Par-8") in { r =>
        import workstealing.Ops._
        implicit val s = s8
        val pr = r.toPar
        pr.fold(0)(_ + _)
      }
    }

  }

}