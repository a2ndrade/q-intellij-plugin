global:1

{
  if[1;global]
  if[1;global:1]
  if[1;global::1]
  if[1;`global]
}

if[1;global]
if[1;global:1]
if[1;global::1]
if[1;`global]

fn:{[<new-name>]
  <new-name>;
  <new-name>:1;
  <new-name>::1;
  `global}

global
global:1
global::1
`global
