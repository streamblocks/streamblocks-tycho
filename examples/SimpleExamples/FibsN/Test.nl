network Test () ==> :

entities 

  f = FibN(N=4);
  p = Print(msg="F= ");
  
structure
  f.Out --> p.In;
  
end