package fitting.proposals

import breeze.linalg.{DenseMatrix, DenseVector}
import fitting.parameters.Sample
import scalismo.sampling.{ProposalGenerator, TransitionProbability}
import scalismo.statisticalmodel.MultivariateNormalDistribution
import scalismo.utils.Random

case class RotationUpdateProposal(stddev: Double)(implicit val rng: Random) extends
  ProposalGenerator[Sample] with TransitionProbability[Sample] {

  val perturbationDistr = new MultivariateNormalDistribution(
    DenseVector.zeros[Double](3),
    DenseMatrix.eye[Double](3) * stddev * stddev)

  def propose(sample: Sample): Sample = {
    val perturbation = perturbationDistr.sample

    val newRotationParameters = (
      sample.parameters.rotationParameters._1 + perturbation(0),
      sample.parameters.rotationParameters._2 + perturbation(1),
      sample.parameters.rotationParameters._3 + perturbation(2)
    )
    val newParameters = sample.parameters.copy(rotationParameters = newRotationParameters)
    sample.copy(generatedBy = s"RotationUpdateProposal ($stddev)", parameters = newParameters)
  }

  override def logTransitionProbability(from: Sample, to: Sample) = {
    val residual = DenseVector(
      to.parameters.rotationParameters._1 - from.parameters.rotationParameters._1,
      to.parameters.rotationParameters._2 - from.parameters.rotationParameters._2,
      to.parameters.rotationParameters._3 - from.parameters.rotationParameters._3
    )
    perturbationDistr.logpdf(residual)
  }
}
