network Top [TypeParamA, TypeParamB] (ValueParamA, ValueParamB) Type1(c:int, a=2, b:int, d:int) in1, Type2 in2 ==> outA, outB :

var
  a := b;
  b := 4;

entities
  nodeA = Add(a=let p1=p2, p2=3 : p1+p2 end);

/*
var
  nTaps = #taps;
  
  function reverse (a) : [a[#a - i] : for i in 1 .. #a] end

  function max (a, b) : if a > b then a else b end end
  
  function fold (a, n)
  var
    k = (#a + n - 1) / n 
  :
    [
      [taps[k * i + j] : for j in 0 .. k - 1, (k * i + j) < nTaps]
    : 
      for i in 0 .. n - 1
    ]
  end
  
  tapSegments = fold(reverse(taps), nUnits);
  nSegs = #tapSegments;

entities
  a = Add(a=5, b=34-2) {a=1; b=3;};
  b = if a=0 then Sub(c=true) else Sub(c=false) end;
  c = [A(), B(), C()];
  d = [Add(a=i+j) : for i in 1..3, for j in 1..3, i<j];

  {
    toolA = 3;
    toolB = 34 - 6;
  }
  
structure
  in1 --> outA {initialtoken = 1; };
  a.a --> b.b {};
  c[2].a --> d[nTaps][1].b;
  if nTaps>3 then a.a --> b.b; end
  if #taps>4 then a.a --> b.b; else a.b --> b.a; end
  foreach i in 1..42, j in 1 do
    c[i].a --> c[i-1].b;
    apa[i].b --> d[i].d;
  end
*/
end
