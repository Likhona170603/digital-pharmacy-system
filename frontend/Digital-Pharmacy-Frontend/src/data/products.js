export const products = [];

export const stockStatus = stock =>
  stock <= 0
    ? { label: 'Out of stock', tone: 'out' }
    : stock <= 10
      ? { label: `Low stock (${stock} left)`, tone: 'low' }
      : { label: 'In stock', tone: 'in' };

export const money = value => `R${Number(value).toFixed(2)}`;
