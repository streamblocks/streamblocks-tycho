network SubGraph [] () in1, in2, in3, in4 ==> out1, out2 :

entities
  nodeA = Add();
  nodeB = Add();

structure
  in1 --> nodeA.A;
  in2 --> nodeA.B;
  nodeA.Result --> out1;
  
  in3 --> nodeB.A;
  in4 --> nodeB.B;
  nodeB.Result --> out2;
end
