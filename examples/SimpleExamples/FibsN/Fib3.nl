network Fib3 () ==> Out:

entities
  add0 = Add();
  add1 = Add();
  z0 = Z(v=1);
  z1 = Z(v=1);
  z2 = Z(v=1);

structure
  z0.Out --> z1.In;
  z0.Out --> add0.A;
  z1.Out --> z2.In;
  z1.Out --> add0.B;
  z2.Out --> add1.B;
  add0.Out --> add1.A;
  add1.Out --> z0.In;
  add1.Out --> Out;
end
