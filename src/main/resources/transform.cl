// transform.cl
__kernel void transform4x4(
    __global const float* inPts,
    __global const float* M,
    __global float* outPts
) {
  int i = get_global_id(0);
  int off = i*4;
  float x = inPts[off], y = inPts[off+1], z = inPts[off+2], w = inPts[off+3];
  outPts[off] = M[0]*x + M[1]*y + M[2]*z + M[3]*w;
  outPts[off+1] = M[4]*x + M[5]*y + M[6]*z + M[7]*w;
  outPts[off+2] = M[8]*x + M[9]*y + M[10]*z+ M[11]*w;
  outPts[off+3] = M[12]*x+ M[13]*y+ M[14]*z+ M[15]*w;
}
