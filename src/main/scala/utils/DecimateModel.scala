package utils

import scalismo.common.interpolation.FieldInterpolator
import scalismo.common.{DiscreteField, Field, RealSpace, UnstructuredPointsDomain}
import scalismo.geometry.{EuclideanVector, Point, _3D}
import scalismo.mesh.boundingSpheres.{ClosestPointInTriangle, ClosestPointIsVertex, ClosestPointOnLine}
import scalismo.mesh.{SurfacePointProperty, TriangleMesh}
import scalismo.statisticalmodel.StatisticalMeshModel
import scalismo.utils.MeshConversion

object DecimateModel {

  class VectorMeshFieldInterpolator(mesh : TriangleMesh[_3D]) extends
    FieldInterpolator[_3D, UnstructuredPointsDomain[_3D], EuclideanVector[_3D]] {

    override def interpolate(df: DiscreteField[_3D,
      UnstructuredPointsDomain[_3D], EuclideanVector[_3D]]): Field[_3D, EuclideanVector[_3D]] = {

      val deformationVecsOnSurface =
        SurfacePointProperty(mesh.triangulation, df.values.toIndexedSeq)

      val f = (pt: Point[_3D]) =>
        mesh.operations.closestPointOnSurface(pt) match {
          case ClosestPointIsVertex(pt, _, id) => deformationVecsOnSurface(id)
          case ClosestPointInTriangle(pt, _, id, bc) =>
            deformationVecsOnSurface(id, bc)
          case ClosestPointOnLine(pt, _, (id0, id1), d) =>
            deformationVecsOnSurface(id0) * d +  deformationVecsOnSurface(id1) *
              (1 - d)
        }
      Field(RealSpace[_3D], f)
    }
  }


  def decimate(model : StatisticalMeshModel, rate : Double=0.99) : StatisticalMeshModel = {
    val refVtk = MeshConversion.meshToVtkPolyData(model.referenceMesh)
    val decimatePro = new vtk.vtkDecimatePro()
    decimatePro.SetTargetReduction(rate)
    decimatePro.SetInputData(refVtk)
    decimatePro.Update()
    val decimatedRefVTK = decimatePro.GetOutput()
    val decimatedMesh = MeshConversion.vtkPolyDataToTriangleMesh(decimatedRefVTK).get

    val newGp = model.gp.interpolate(new VectorMeshFieldInterpolator(model.referenceMesh))
    val newSSM = StatisticalMeshModel(decimatedMesh, newGp)

    newSSM

  }
}
