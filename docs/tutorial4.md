# Building a shape model from data

The goal in this tutorial is to learn how to build a Statistical Shape Model
from meshes in correspondence.


##### Preparation

As in the previous tutorials, we start by importing some commonly used objects and initializing the system.

```scala
import scalismo.geometry._
import scalismo.common._
import scalismo.ui.api._
import scalismo.mesh._
import scalismo.io.{StatisticalModelIO, MeshIO}
import scalismo.statisticalmodel._
import scalismo.registration._
import scalismo.statisticalmodel.dataset._
import java.io.File

scalismo.initialize()
implicit val rng = scalismo.utils.Random(42)

val ui = ScalismoUI()

```

### Loading and prepossessing a data set:

Let us load (and visualize) a set of meshes based on which we would like to model shape variation:

```scala
val dsGroup = ui.createGroup("datasets")

val meshFiles = new File("datasets/nonAlignedFemurs/").listFiles.sorted

val (meshes, meshViews) = meshFiles.map(meshFile => {
  val mesh = MeshIO.readMesh(meshFile).get 
  val meshView = ui.show(dsGroup, mesh, meshFile.getName)
  (mesh, meshView) // return a tuple of the mesh and the associated view
}).unzip // take the tuples apart, to get a sequence of meshes and one of meshViews 

```

You immediately see that the meshes are not aligned. What you cannot see, but is
very important for this tutorial, is
that the meshes are **in correspondence**.
This means that for every point on one of the meshes, we can identify the corresponding point on
other meshes.  Corresponding points are identified by the same point id.


#### Rigidly aligning the data:

In order to study shape variations, we need to eliminate variations due to
relative spatial displacement of the shapes (rotation and translation).
This can be achieved by selecting one of the meshes as a reference,
to which the rest of the datasets are aligned.
In this example here, we take the first mesh as a reference.

```scala
val reference = meshes.head
val toAlign : IndexedSeq[TriangleMesh[_3D]] = meshes.tail

```

Given that our data set is in correspondence, we can specify a set of point
identifiers, to locate corresponding points on the meshes.

```scala
val pointIds = IndexedSeq(1092, 1395, 2059, 4460, 4566, 2766)
val refLandmarks = pointIds.map{id => Landmark(s"L_$id", reference.pointSet.point(PointId(id))) }

```

After locating the landmark positions on the reference, we iterate on each remaining data item, identify the corresponding landmark points and then rigidly align the mesh to the reference.

```scala
val alignedMeshes = toAlign.map { mesh =>    
     val landmarks = pointIds.map{id => Landmark("L_"+id, mesh.pointSet.point(PointId(id)))}
     val rigidTrans = LandmarkRegistration.rigid3DLandmarkRegistration(landmarks, refLandmarks, center = Point(0,0,0))
     mesh.transform(rigidTrans)
}

```

Now, the IndexedSeq of triangle meshes *alignedMeshes* contains the femurs that are aligned to the reference mesh.

To verify this, we show one (the last) of the aligned meshes and compare it to the reference (which should 0.ply)

```scala
ui.show(ui.createGroup("aligned?"), alignedMeshes.last, "aligned mesh")

```


### Building the PCA model

Now that we have the meshes aligned to the reference, we can build our PCA model.


The *DataCollection* class in Scalismo allows grouping together a dataset of meshes in correspondence,
in order to make collective operations on such sets easier.

We can create a *DataCollection* by providing a reference mesh, and
a sequence of meshes, which are in correspondence with this reference.

```scala
val dc = DataCollection.fromMeshSequence(reference, alignedMeshes)._1.get

```

Now that we have our data collection, we can build a shape model as follows:

```scala
val model = StatisticalMeshModel.createUsingPCA(dc).get

val modelGroup = ui.createGroup("pca model")
ui.show(modelGroup, model, "pca model")

```

Finally, we save the model to file in order to be able to use in the following tutorial.


```scala
StatisticalModelIO.writeStatisticalMeshModel(model, new File("datasets/my_pcamodel.h5"))

```

