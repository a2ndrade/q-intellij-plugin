\d .ns

global:1

{
  if[1;global]
  if[1;`global]
}

if[1;global]
if[1;`global]

fnA:{[<new-name>]
  <new-name>;
  `global}

fnB:{[a;b;c]
  global;
  `global}

global
`global
