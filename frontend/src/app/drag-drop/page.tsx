'use client';

import * as React from 'react';
import {
  Box,
  Button,
  Card,
  CardContent,
  Container,
  Stack,
  Typography,
} from '@mui/material';

// カード1枚分の型。createdNoは作成順（表示順が変わっても固定）
interface Item {
  id: number; // 一意ID（表示順入れ替えに使用）
  createdNo: number; // 作成順（カードに表示）
}

export default function Page() {
  const [items, setItems] = React.useState<Item[]>([
    { id: 1, createdNo: 1 },
    { id: 2, createdNo: 2 },
    { id: 3, createdNo: 3 },
  ]);
  const [nextId, setNextId] = React.useState(4);
  const [nextCreatedNo, setNextCreatedNo] = React.useState(4);

  // ドラッグ中のカードIDと、ホバー中の挿入インデックス
  const [draggingId, setDraggingId] = React.useState<number | null>(null);
  const [hoverInsertIndex, setHoverInsertIndex] = React.useState<number | null>(null);

  const onAddCard = () => {
    setItems((prev) => [...prev, { id: nextId, createdNo: nextCreatedNo }]);
    setNextId((v) => v + 1);
    setNextCreatedNo((v) => v + 1);
  };

  // 配列の要素を from から to に移動
  const moveItem = (arr: Item[], from: number, to: number) => {
    const copy = arr.slice();
    const [moved] = copy.splice(from, 1);
    copy.splice(to, 0, moved);
    return copy;
  };

  const handleDragStart = (e: React.DragEvent, id: number) => {
    e.dataTransfer.setData('text/plain', String(id));
    e.dataTransfer.effectAllowed = 'move';
    setDraggingId(id);
  };

  const handleDragEnd = () => {
    setDraggingId(null);
    setHoverInsertIndex(null);
  };

  const handleZoneDragOver = (e: React.DragEvent, insertIndex: number) => {
    e.preventDefault(); // これがないとdropできない
    e.dataTransfer.dropEffect = 'move';
    setHoverInsertIndex(insertIndex);
  };

  const handleZoneDrop = (e: React.DragEvent, insertIndex: number) => {
    e.preventDefault();
    const idStr = e.dataTransfer.getData('text/plain');
    const id = Number(idStr);
    const fromIndex = items.findIndex((it) => it.id === id);
    if (fromIndex === -1) return;

    // 元の位置より右（下）に挿入する場合は、取り除いた分だけインデックスを1つ詰める
    let toIndex = insertIndex;
    if (fromIndex < insertIndex) {
      toIndex = insertIndex - 1;
    }
    if (toIndex === fromIndex || toIndex < 0) {
      setHoverInsertIndex(null);
      return;
    }

    setItems((prev) => moveItem(prev, fromIndex, toIndex));
    setHoverInsertIndex(null);
    setDraggingId(null);
  };

  // ドロップ領域コンポーネント（点線・角丸）
  const DropZone: React.FC<{ index: number }> = ({ index }) => {
    const active = hoverInsertIndex === index;
    return (
      <Box
        onDragOver={(e) => handleZoneDragOver(e, index)}
        onDrop={(e) => handleZoneDrop(e, index)}
        onDragLeave={() => setHoverInsertIndex((cur) => (cur === index ? null : cur))}
        sx={{
          border: '2px dashed',
          borderColor: active ? 'primary.main' : 'divider',
          borderRadius: 2,
          height: 44,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          bgcolor: active ? 'action.hover' : 'transparent',
          transition: 'all .15s ease',
          mb: 1,
        }}
      >
        <Typography variant="body2" color="text.secondary">
          {active ? 'ここにドロップ' : 'ドロップ領域'}
        </Typography>
      </Box>
    );
  };

  return (
    <Container maxWidth="sm" sx={{ py: 4 }}>
      {/* ヘッダーバー：タイトル + 追加ボタン（横並び） */}
      <Stack direction="row" alignItems="center" justifyContent="space-between" mb={2}>
        <Typography variant="h5" fontWeight={700}>
          ドラッグ&ドロップ（MUI）
        </Typography>
        <Button variant="contained" onClick={onAddCard}>
          カードを追加
        </Button>
      </Stack>

      {/* 縦並びリスト：各カードの前後にドロップ領域を配置 */}
      <Stack>
        {/* 先頭用ドロップ領域（index = 0） */}
        <DropZone index={0} />

        {items.map((item, i) => (
          <React.Fragment key={item.id}>
            <Card
              draggable
              onDragStart={(e) => handleDragStart(e, item.id)}
              onDragEnd={handleDragEnd}
              sx={{
                mb: 1,
                cursor: 'grab',
                boxShadow: draggingId === item.id ? 6 : 1,
                border: draggingId === item.id ? 2 : 1,
                borderColor: draggingId === item.id ? 'primary.main' : 'divider',
              }}
              aria-grabbed={draggingId === item.id}
              aria-label={`カード ${item.createdNo}`}
              role="listitem"
            >
              <CardContent>
                <Typography variant="h6">Card #{item.createdNo}</Typography>
                <Typography variant="body2" color="text.secondary">
                  作成順は固定（並べ替えても番号は変わりません）
                </Typography>
              </CardContent>
            </Card>

            {/* 各カードの直後のドロップ領域（index = i+1） */}
            <DropZone index={i + 1} />
          </React.Fragment>
        ))}
      </Stack>

      {/* 補足情報 */}
      <Box mt={3}>
        <Typography variant="caption" color="text.secondary">
          ・HTML5のDrag & Drop APIのみで実装（追加ライブラリ不要）
        </Typography>
      </Box>
    </Container>
  );
}
