'use client';

import React, { useRef, useState } from 'react';

type Card = {
  id: string;
  createdNo: number; // 作成順を示す番号（固定表示用）
};

export default function Page() {
  const [cards, setCards] = useState<Card[]>([
    { id: 'c1', createdNo: 1 },
    { id: 'c2', createdNo: 2 },
    { id: 'c3', createdNo: 3 },
  ]);
  const [nextNo, setNextNo] = useState(4);

  // 現在ドラッグ中のカードID
  const draggingId = useRef<string | null>(null);

  // 現在ホバー中のドロップインデックス（見た目ハイライト用）
  const [hoverIndex, setHoverIndex] = useState<number | null>(null);

  // 指定位置へカードを挿入する
  const handleDropAt = (insertIndex: number) => {
    const srcId = draggingId.current;
    if (!srcId) return;

    setCards(prev => {
      const from = prev.findIndex(c => c.id === srcId);
      if (from === -1) return prev.slice();

      const arr = prev.slice();
      const [moved] = arr.splice(from, 1);

      // 取り除いた後に右側へ挿入する場合、インデックスが1つ左に詰まるので補正
      let idx = insertIndex;
      if (idx > from) idx -= 1;

      arr.splice(idx, 0, moved);
      return arr;
    });

    setHoverIndex(null);
    draggingId.current = null;
  };

  const handleAddCard = () => {
    setCards(prev => [...prev, { id: `c${crypto.randomUUID()}`, createdNo: nextNo }]);
    setNextNo(n => n + 1);
  };

  // ドロップゾーン（挿入位置を表すコンポーネント）
  const DropZone: React.FC<{ index: number }> = ({ index }) => {
    const active = hoverIndex === index;
    return (
      <div
        onDragOver={(e) => {
          e.preventDefault(); // これが無いと drop が発火しない
        }}
        onDragEnter={(e) => {
          e.preventDefault();
          setHoverIndex(index);
        }}
        onDragLeave={() => setHoverIndex(null)}
        onDrop={() => handleDropAt(index)}
        style={{
          height: 32,
          margin: '8px 0',
          border: '2px dotted #999',
          borderRadius: 12,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          opacity: active ? 1 : 0.6,
          background: active ? 'rgba(0,0,0,0.05)' : 'transparent',
          transition: 'background 120ms ease, opacity 120ms ease',
          userSelect: 'none',
        }}
        aria-label={`dropzone-${index}`}
      >
        <span style={{ fontSize: 12, color: '#666' }}>
          ここにドロップして挿入
        </span>
      </div>
    );
  };

  // 表示用のカード
  const CardView: React.FC<{ card: Card; index: number }> = ({ card }) => {
    return (
      <div
        draggable
        onDragStart={(e) => {
          draggingId.current = card.id;
          e.dataTransfer.effectAllowed = 'move';
          // Firefox対策: setDataが無いとdragできないケースがある
          e.dataTransfer.setData('text/plain', card.id);
        }}
        onDragEnd={() => {
          draggingId.current = null;
          setHoverIndex(null);
        }}
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
        }}
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
        {/* ヘッダー & 右寄せボタン（横配置） */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            marginBottom: 16,
          }}
        >
          <h1 style={{ margin: 0, fontSize: 20, fontWeight: 700 }}>ドラッグ＆ドロップ例</h1>
          <div style={{ marginLeft: 'auto', display: 'flex', gap: 8 }}>
            <button
              type="button"
              onClick={handleAddCard}
              style={{
                padding: '10px 14px',
                borderRadius: 10,
                border: '1px solid #ddd',
                background: '#fff',
                boxShadow: '0 2px 6px rgba(0,0,0,0.06)',
                cursor: 'pointer',
                fontWeight: 600,
              }}
              title="右側のボタン（横配置）でカード追加"
            >
              カードを追加
            </button>
          </div>
        </div>

        {/* リスト本体：各カードの前後にドロップ領域 */}
        <div style={{ background: '#ffffff', border: '1px solid #eee', borderRadius: 14, padding: 16 }}>
          {/* 先頭のドロップゾーン（index=0 に挿入） */}
          <DropZone index={0} />

          {cards.map((card, i) => (
            <React.Fragment key={card.id}>
              <CardView card={card} index={i} />
              {/* 各カードの後ろにもドロップゾーン（挿入先 index = i+1） */}
              <DropZone index={i + 1} />
            </React.Fragment>
          ))}
        </div>

        <p style={{ color: '#666', fontSize: 12, marginTop: 12 }}>
          ※ 各カードの上下にある点線の角丸枠がドロップ領域です。カードの左の数字は「作成順（固定表示）」です。
        </p>
      </div>
    </main>
  );
}
