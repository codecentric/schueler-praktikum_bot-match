-- Wandelt raw HTML <img src="..." width="..."> in echte Pandoc-Image-Elemente um.
-- Noetig, weil pandoc rohe HTML-<img>-Tags beim PDF/LaTeX-Export sonst stillschweigend verwirft.
local function img_to_element(html)
  local src = html:match('src="([^"]+)"')
  if not src then return nil end
  local alt = html:match('alt="([^"]*)"') or ""
  local width = html:match('width="([^"]+)"')
  local attr = pandoc.Attr("", {}, width and {{"width", width .. "px"}} or {})
  return pandoc.Image({ pandoc.Str(alt) }, src, "", attr)
end

function RawBlock(el)
  if el.format == "html" and el.text:match("^%s*<img ") then
    local img = img_to_element(el.text)
    if img then
      return pandoc.Para({ img })
    end
  end
  return el
end

function RawInline(el)
  if el.format == "html" and el.text:match("^%s*<img ") then
    local img = img_to_element(el.text)
    if img then
      return img
    end
  end
  return el
end
