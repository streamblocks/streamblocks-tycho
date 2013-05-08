network FibsN (N) ==> Out:

entities
  add = [Add() : for i in 0 .. N-2];
  z = [Z(v=1) : for i in 0 .. N-1];

structure
  z[0].Out --> add[0].A;
  for i in 0 .. N-2 do
    z[i].Out --> z[i+1].In;
    z[i+1].Out --> add[i].B;
    if i < N-2 then
      add[i].Out --> add[i+1].A;
    end
  end
  add[N-2].Out --> Out;
  add[N-2].Out --> z[0].In;
end
