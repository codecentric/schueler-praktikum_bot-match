function Pandoc(doc)
  local blocks = doc.blocks
  local out = {}
  local i = 1
  local first = true
  while i <= #blocks do
    local b = blocks[i]
    if b.t == "Header" and b.level == 3 then
      if not first then
        table.insert(out, pandoc.RawBlock('latex', '\\clearpage'))
      end
      first = false
      table.insert(out, pandoc.RawBlock('latex', '\\begin{storybox}'))
      table.insert(out, b)
      i = i + 1
      while i <= #blocks and not (blocks[i].t == "Header" and blocks[i].level <= 3) do
        table.insert(out, blocks[i])
        i = i + 1
      end
      table.insert(out, pandoc.RawBlock('latex', '\\end{storybox}'))
    elseif b.t == "Header" and b.level <= 2 then
      -- Epic-Überschriften (H1/H2) nicht mit ausdrucken, nur Stories zählen als Seiten
      i = i + 1
    else
      i = i + 1
    end
  end
  doc.blocks = out
  return doc
end
