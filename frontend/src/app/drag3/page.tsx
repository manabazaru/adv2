'use client';

import React, { useEffect, useRef, useState } from 'react';

type Card = {
  id: string;
  createdNo: number; // 作成順（表示固定）
};

type DragKind = 'move-card' | 'new-card';

export default function Page() {
  const [cards, setCards] = useState<Card[]>([
    { id: 'c1', createdNo: 1 },
    { id: 'c2', createdNo: 2 },
    { id: 'c3', createdNo: 3 },
  ]);
  const [nextNo, setNextNo] = useState(4);

  // 直近で「追加」されたカードID（スクロール＆フォーカス対象）
  const [lastAddedId, setLastAddedId] = useState<string | null>(null);

  // 各カードのDOM参照を保持するマップ
  const cardNodeMapRef = useRef<Record<string, HTMLDivElement | null>>({});

  // 何をドラッグ中か
  const draggingKind = useRef<DragKind | null>(null);
  const draggingCardId = useRef<string | null>(null);

  // ドロップゾーンのハイライト制御
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);
  const [hoverKind, setHoverKind] = useState<DragKind | null>(null);

  // ===== スクロール＆フォーカス =====
  useEffect(() => {
    if (!lastAddedId) return;
    // レンダ後にDOMが確実に揃うように requestAnimationFrame を使用
    const id = lastAddedId;
    const raf = requestAnimationFrame(() => {
      const el = cardNodeMapRef.current[id];
      if (el) {
        el.scrollIntoView({ behavior: 'smooth', block: 'center' });
        // スクロールした上でフォーカスも与える（アクセシビリティ向上）
        el.focus({ preventScroll: true });
      }
    });
    return () => cancelAnimationFrame(raf);
  }, [lastAddedId, cards.length]);

  // ===== 並べ替え（移動） =====
  const moveCardAt = (insertIndex: number, srcId: string) => {
    setCards(prev => {
      const from = prev.findIndex(c => c.id === srcId);
      if (from === -1) return prev.slice();
      const arr = prev.slice();
      const [moved] = arr.splice(from, 1);
      let idx = insertIndex;
      if (idx > from) idx -= 1; // 右側へ差し込む場合の補正
      arr.splice(idx, 0, moved);
      return arr;
    });
  };

  // ===== 追加（ドラッグ or クリック） =====
  const addNewCardAt = (insertIndex: number) => {
    const newId = `c-${crypto.randomUUID()}`;
    setCards(prev => {
      const arr = prev.slice();
      arr.splice(insertIndex, 0, {
        id: newId,
        createdNo: nextNo,
      });
      return arr;
    });
    setNextNo(n => n + 1);
    setLastAddedId(newId); // 追加直後にスクロール先を設定
  };

  const handleAddToEnd = () => addNewCardAt(cards.length);

  const resetDragState = () => {
    draggingKind.current = null;
    draggingCardId.current = null;
    setHoverIndex(null);
    setHoverKind(null);
  };

  const handleDropAt = (insertIndex: number, e: React.DragEvent) => {
    e.preventDefault();
    const kind = draggingKind.current;
    if (!kind) return;

    if (kind === 'move-card') {
      const id =
        draggingCardId.current ||
        e.dataTransfer.getData('text/x-card-id') ||
        '';
      if (id) moveCardAt(insertIndex, id);
    } else if (kind === 'new-card') {
      addNewCardAt(insertIndex);
    }
    resetDragState();
  };

  // ===== UI パーツ =====

  // ドロップゾーン
  const DropZone: React.FC<{ index: number }> = ({ index }) => {
    const active = hoverIndex === index;
    const isNew = active && hoverKind === 'new-card';

    return (
      <div
        onDragOver={(e) => {
          e.preventDefault();
          e.dataTransfer.dropEffect = 'move';
        }}
        onDragEnter={(e) => {
          e.preventDefault();
          setHoverIndex(index);
          setHoverKind(draggingKind.current);
        }}
        onDragLeave={() => {
          setHoverIndex(null);
          setHoverKind(null);
        }}
        onDrop={(e) => handleDropAt(index, e)}
        style={{
          height: 34,
          margin: '10px 0',
          border: '2px dotted #999',
          borderRadius: 12,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          userSelect: 'none',
          transition: 'all 120ms ease',
          opacity: active ? 1 : 0.6,
          background: active
            ? isNew
              ? 'rgba(0, 160, 0, 0.07)'
              : 'rgba(0,0,0,0.05)'
            : 'transparent',
        }}
        aria-label={`dropzone-${index}`}
      >
        <span style={{ fontSize: 12, color: '#666' }}>
          {hoverKind === 'new-card' ? 'ここで新規カードを挿入' : 'ここにドロップして挿入'}
        </span>
      </div>
    );
  };

  // カード
  const CardView: React.FC<{ card: Card }> = ({ card }) => {
    return (
      <div
        ref={(el) => {
          cardNodeMapRef.current[card.id] = el;
        }}
        tabIndex={-1} // フォーカス可能にする
        draggable
        onDragStart={(e) => {
          draggingKind.current = 'move-card';
          draggingCardId.current = card.id;
          e.dataTransfer.effectAllowed = 'move';
          e.dataTransfer.setData('text/x-dnd-kind', 'move-card');
          e.dataTransfer.setData('text/x-card-id', card.id); // Firefox対策
        }}
        onDragEnd={resetDragState}
        style={{
          padding: 16,
          border: '1px solid #ddd',
          borderRadius: 12,
          background: '#fff',
          boxShadow: '0 2px 6px rgba(0,0,0,0.06)',
          cursor: 'grab',
          display: 'flex',
          alignItems: 'center',
          gap: 12,
          outline: 'none',
        }}
        role="group"
        aria-label={`カード ${card.createdNo}`}
      >
        <div
          aria-hidden
          style={{
            width: 28,
            height: 28,
            borderRadius: 8,
            background: '#f2f2f2',
            display: 'grid',
            placeItems: 'center',
            fontWeight: 700,
            color: '#333',
          }}
          title="作成順（固定）"
        >
          {card.createdNo}
        </div>
        <div style={{ fontWeight: 600, color: '#222' }}>
          カード ID: <code>{card.id.slice(0, 8)}</code>
        </div>
        <div style={{ marginLeft: 'auto', color: '#888', fontSize: 12 }}>
          ドラッグして移動
        </div>
      </div>
    );
  };

  // ドラッグ可能な “新規カード” ボタン
  const DraggableAddButton: React.FC = () => {
    return (
      <button
        type="button"
        onClick={handleAddToEnd}
        draggable
        onDragStart={(e) => {
          draggingKind.current = 'new-card';
          draggingCardId.current = null;
          e.dataTransfer.effectAllowed = 'copyMove';
          e.dataTransfer.setData('text/x-dnd-kind', 'new-card');

          // 目印用のドラッグイメージ（任意）
          const ghost = document.createElement('div');
          ghost.style.padding = '8px 12px';
          ghost.style.background = '#0a7';
          ghost.style.color = '#fff';
          ghost.style.borderRadius = '8px';
          ghost.style.fontWeight = '700';
          ghost.style.position = 'absolute';
          ghost.style.top = '-9999px';
          ghost.textContent = '新規カードを追加';
          document.body.appendChild(ghost);
          e.dataTransfer.setDragImage(ghost, 10, 10);
          setTimeout(() => document.body.removeChild(ghost), 0);
        }}
        onDragEnd={resetDragState}
        title="クリック: 末尾に追加 / ドラッグ: 任意の位置に追加"
        style={{
          padding: '10px 14px',
          borderRadius: 10,
          border: '1px solid #0a7',
          background: '#0a7',
          color: '#fff',
          boxShadow: '0 2px 6px rgba(0,0,0,0.15)',
          cursor: 'grab',
          fontWeight: 700,
          letterSpacing: 0.2,
        }}
      >
        ＋ 新規カード
      </button>
    );
  };

  // ===== レイアウト =====
  return (
    <main
      style={{
        minHeight: '100svh',
        display: 'grid',
        placeItems: 'start center',
        background: '#fafafa',
        padding: 24,
      }}
    >
      <div style={{ width: 'min(720px, 92vw)' }}>
        {/* ヘッダー */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            marginBottom: 16,
          }}
        >
          <h1 style={{ margin: 0, fontSize: 20, fontWeight: 700 }}>
            ドラッグ＆ドロップ例（任意位置に“新規カード”を追加）
          </h1>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <DraggableAddButton />
          </div>
        </div>

        {/* リスト：各カードの前後にドロップゾーン */}
        <div style={{ background: '#ffffff', border: '1px solid #eee', borderRadius: 14, padding: 16 }}>
          {/* 先頭の挿入位置 */}
          <DropZone index={0} />
          {cards.map((card, i) => (
            <React.Fragment key={card.id}>
              <CardView card={card} />
              <DropZone index={i + 1} />
            </React.Fragment>
          ))}
        </div>

        <ul style={{ color: '#666', fontSize: 12, marginTop: 12, lineHeight: 1.6 }}>
          <li>各カードの上下にある点線の角丸枠がドロップ領域です。</li>
          <li>緑色ハイライト時にドロップすると、その位置に<b>新規カード</b>が挿入されます。</li>
          <li>「＋ 新規カード」ボタンは、<b>クリックで末尾追加 / ドラッグで任意位置に追加</b>できます。</li>
          <li>カード左の数字は「作成順（固定表示）」で、並び替えても変わりません。</li>
          <li>新規追加直後は、該当カードへ自動スクロール＆フォーカスします。</li>
        </ul>
      </div>
    </main>
  );
}
