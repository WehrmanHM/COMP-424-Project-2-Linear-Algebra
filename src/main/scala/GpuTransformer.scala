import org.jocl.*

import scala.io.Source

object GpuTransformer {
  private lazy val (context, queue) = OpenCLContext.initGPU()
  
  private val (program, kernel) = {
    val src = Source.fromResource("transform.cl").mkString
    val p   = CL.clCreateProgramWithSource(context, 1, Array(src), null, null)
    CL.clBuildProgram(p, 0, null, null, null, null)
    val k   = CL.clCreateKernel(p, "transform4x4", null)
    (p, k)
  }

  def transform(inPts: Array[Float], M: Array[Float]): Array[Float] = {
    val pointCount = inPts.length / 4
    val outPts = new Array[Float](inPts.length)

    // create buffers:
    // bufIn holds our flattened points
    // bufMatrix holds the matrix
    // bufOut holds the transformed flattened points
    // bufIn and bufMatrix are created and filled on the GPU immediately; bufOut is just allocated and will be filled during execution
    val bufIn  = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY  | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * inPts.length, Pointer.to(inPts), null)
    val bufMatrix   = CL.clCreateBuffer(context, CL.CL_MEM_READ_ONLY  | CL.CL_MEM_COPY_HOST_PTR, Sizeof.cl_float * 16, Pointer.to(M), null)
    val bufOut = CL.clCreateBuffer(context, CL.CL_MEM_WRITE_ONLY, Sizeof.cl_float * outPts.length, null, null)

    // associate .cl pointers with buffers on the GPU
    // 0 = inPts
    // 1 = Matrix (M)
    // 2 = outPts
    CL.clSetKernelArg(kernel, 0, Sizeof.cl_mem, Pointer.to(bufIn))
    CL.clSetKernelArg(kernel, 1, Sizeof.cl_mem, Pointer.to(bufMatrix))
    CL.clSetKernelArg(kernel, 2, Sizeof.cl_mem, Pointer.to(bufOut))
    
    // enqueue the kernel: all points are processed in parallel on the GPU so we start it all at once
    // important arguments:
    // - 1 -> working in 1 dimension (scalar operations)
    // - Array(pointCount.toLong) -> how much work there is to do
    // - 0 -> no events waiting after this
    CL.clEnqueueNDRangeKernel(queue, kernel, 1, null, Array(pointCount.toLong), null, 0, null, null)
    // set up our read buffer, which moves information from bufOut back to memory
    // we provide Pointer.to(outPts) so it knows where to go
    CL.clEnqueueReadBuffer(queue, bufOut, CL.CL_TRUE, 0, Sizeof.cl_float * outPts.length, Pointer.to(outPts), 0, null, null)
    
    // clean up
    CL.clReleaseMemObject(bufIn)
    CL.clReleaseMemObject(bufMatrix)
    CL.clReleaseMemObject(bufOut)

    // return!
    outPts
  }
}