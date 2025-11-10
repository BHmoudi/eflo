'use client';

import React, { useEffect, useRef, useState } from 'react';
import grapesjs from 'grapesjs';
import 'grapesjs/dist/css/grapes.min.css';
import gjsPresetWebpage from 'grapesjs-preset-webpage';
import Editor from '@monaco-editor/react';
import { DndContext, DragEndEvent } from '@dnd-kit/core';
import Split from 'react-split';
import { Code, Eye, Layout, Download } from 'lucide-react';
import VariablePanel, { AVAILABLE_VARIABLES } from './VariablePanel';
import Button from '@/components/ui/Button';

interface TemplateEditorProps {
  initialHtml?: string;
  onChange?: (html: string, css: string) => void;
  onSave?: (html: string, css: string) => void;
}

export default function TemplateEditor({ initialHtml = '', onChange, onSave }: TemplateEditorProps) {
  const [mode, setMode] = useState<'visual' | 'code'>('visual');
  const [htmlContent, setHtmlContent] = useState(initialHtml);
  const [cssContent, setCssContent] = useState('');
  const editorRef = useRef<any>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (mode === 'visual' && containerRef.current && !editorRef.current) {
      const editor = grapesjs.init({
        container: containerRef.current,
        height: '100%',
        width: 'auto',
        storageManager: false,
        plugins: [gjsPresetWebpage],
        pluginsOpts: {
          gjsPresetWebpage: {
            blocksBasicOpts: {
              blocks: ['text', 'image', 'video', 'map'],
              flexGrid: true,
            },
          },
        },
        canvas: {
          styles: [
            'https://cdn.jsdelivr.net/npm/tailwindcss@2.2.19/dist/tailwind.min.css',
          ],
        },
        blockManager: {
          appendTo: '#blocks-container',
        },
        panels: {
          defaults: [
            {
              id: 'basic-actions',
              el: '.panel__basic-actions',
              buttons: [
                {
                  id: 'visibility',
                  active: true,
                  className: 'btn-toggle-borders',
                  label: '<i class="fa fa-clone"></i>',
                  command: 'sw-visibility',
                },
              ],
            },
            {
              id: 'devices',
              el: '.panel__devices',
              buttons: [
                {
                  id: 'device-desktop',
                  label: '<i class="fa fa-desktop"></i>',
                  command: 'set-device-desktop',
                  active: true,
                },
                {
                  id: 'device-mobile',
                  label: '<i class="fa fa-mobile"></i>',
                  command: 'set-device-mobile',
                },
              ],
            },
          ],
        },
      });

      // Load initial content
      if (initialHtml) {
        editor.setComponents(initialHtml);
      }

      // Add custom variable insertion command
      editor.Commands.add('insert-variable', {
        run(editor: any, sender: any, opts: any) {
          const variable = opts.variable;
          const selected = editor.getSelected();

          if (selected && selected.is('text')) {
            const content = selected.get('content') || '';
            selected.set('content', content + `{{${variable}}}`);
          } else {
            editor.addComponents({
              tagName: 'span',
              type: 'text',
              content: `{{${variable}}}`,
              style: {
                color: '#2563eb',
                fontWeight: 'bold',
                backgroundColor: '#dbeafe',
                padding: '2px 6px',
                borderRadius: '4px',
              },
            });
          }
        },
      });

      // Listen for changes
      editor.on('update', () => {
        const html = editor.getHtml();
        const css = editor.getCss();
        setHtmlContent(html);
        setCssContent(css);
        onChange?.(html, css);
      });

      editorRef.current = editor;

      return () => {
        if (editorRef.current) {
          editorRef.current.destroy();
          editorRef.current = null;
        }
      };
    }
  }, [mode]);

  const handleDragEnd = (event: DragEndEvent) => {
    const { active } = event;
    const variable = AVAILABLE_VARIABLES.find(v => v.name === active.id);

    if (variable && editorRef.current) {
      if (mode === 'visual') {
        editorRef.current.runCommand('insert-variable', { variable: variable.name });
      } else {
        // Insert at cursor in Monaco
        setHtmlContent(prev => prev + `{{${variable.name}}}`);
      }
    }
  };

  const handleMonacoChange = (value: string | undefined) => {
    const newHtml = value || '';
    setHtmlContent(newHtml);
    onChange?.(newHtml, cssContent);
  };

  const handleSave = () => {
    if (mode === 'visual' && editorRef.current) {
      const html = editorRef.current.getHtml();
      const css = editorRef.current.getCss();
      onSave?.(html, css);
    } else {
      onSave?.(htmlContent, cssContent);
    }
  };

  const switchToVisual = () => {
    if (editorRef.current && mode === 'code') {
      editorRef.current.setComponents(htmlContent);
    }
    setMode('visual');
  };

  const switchToCode = () => {
    if (editorRef.current && mode === 'visual') {
      setHtmlContent(editorRef.current.getHtml());
      setCssContent(editorRef.current.getCss());
    }
    setMode('code');
  };

  return (
    <DndContext onDragEnd={handleDragEnd}>
      <div className="flex flex-col h-full">
        {/* Toolbar */}
        <div className="bg-white border-b border-gray-200 px-4 py-3">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <button
                onClick={switchToVisual}
                className={`px-4 py-2 rounded-lg flex items-center gap-2 transition-colors ${
                  mode === 'visual'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                <Layout className="w-4 h-4" />
                Visual Editor
              </button>
              <button
                onClick={switchToCode}
                className={`px-4 py-2 rounded-lg flex items-center gap-2 transition-colors ${
                  mode === 'code'
                    ? 'bg-blue-600 text-white'
                    : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                }`}
              >
                <Code className="w-4 h-4" />
                Code Editor
              </button>
            </div>

            <div className="flex items-center gap-3">
              <Button variant="outline" size="sm" onClick={() => console.log('Preview')}>
                <Eye className="w-4 h-4 mr-2" />
                Preview
              </Button>
              <Button variant="primary" size="sm" onClick={handleSave}>
                <Download className="w-4 h-4 mr-2" />
                Save Template
              </Button>
            </div>
          </div>
        </div>

        {/* Editor Area */}
        <div className="flex-1 flex overflow-hidden">
          <VariablePanel />

          <div className="flex-1 flex flex-col">
            {mode === 'visual' ? (
              <>
                <div className="panel__devices bg-gray-100 p-2 border-b border-gray-200"></div>
                <div className="panel__basic-actions bg-gray-100 p-2 border-b border-gray-200"></div>
                <div id="blocks-container" className="bg-gray-50 border-b border-gray-200 p-2"></div>
                <div ref={containerRef} className="flex-1" />
              </>
            ) : (
              <Split
                className="flex-1 flex"
                sizes={[60, 40]}
                minSize={200}
                gutterSize={8}
                direction="horizontal"
              >
                <div className="flex flex-col">
                  <div className="bg-gray-100 px-4 py-2 border-b border-gray-200">
                    <h3 className="text-sm font-semibold text-gray-700">HTML Template</h3>
                  </div>
                  <Editor
                    height="100%"
                    defaultLanguage="html"
                    value={htmlContent}
                    onChange={handleMonacoChange}
                    theme="vs-light"
                    options={{
                      minimap: { enabled: false },
                      fontSize: 13,
                      wordWrap: 'on',
                      formatOnPaste: true,
                      formatOnType: true,
                      suggest: {
                        snippetsPreventQuickSuggestions: false,
                      },
                    }}
                  />
                </div>
                <div className="flex flex-col border-l border-gray-200">
                  <div className="bg-gray-100 px-4 py-2 border-b border-gray-200">
                    <h3 className="text-sm font-semibold text-gray-700">CSS Styles (Optional)</h3>
                  </div>
                  <Editor
                    height="100%"
                    defaultLanguage="css"
                    value={cssContent}
                    onChange={(value) => setCssContent(value || '')}
                    theme="vs-light"
                    options={{
                      minimap: { enabled: false },
                      fontSize: 13,
                      wordWrap: 'on',
                    }}
                  />
                </div>
              </Split>
            )}
          </div>
        </div>
      </div>
    </DndContext>
  );
}
