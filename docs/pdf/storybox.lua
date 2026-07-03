function Pandoc(doc)
  local blocks = doc.blocks
  local out = {}
  local i = 1
  while i <= #blocks do
    local b = blocks[i]
    if b.t == "Header" and b.level == 3 then
      table.insert(out, pandoc.RawBlock('latex', '\\begin{storybox}'))
      table.insert(out, b)
      i = i + 1
      while i <= #blocks and not (blocks[i].t == "Header" and blocks[i].level <= 3) do
        table.insert(out, blocks[i])
        i = i + 1
      end
      table.insert(out, pandoc.RawBlock('latex', '\\end{storybox}'))
    else
      table.insert(out, b)
      i = i + 1
    end
  end
  doc.blocks = out
  return doc
end
